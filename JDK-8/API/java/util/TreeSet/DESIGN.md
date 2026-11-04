# java.util.TreeSet 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。构造器矩阵、null 元素语义、NavigableSet 边界、范围视图活对象写通道、
> 倒序视图、迭代器写通道 + fail-fast（CME/ISE/NSEE）、toArray 契约、跨实现 equals、clone/序列化、
> 状态不变量全部补齐。TreeSet 有序，可断言顺序；跨实现 equals 只依赖内容相等，排序无关。

## 1. API 提供了哪些功能

- **构造器（4）**：`TreeSet()`、`TreeSet(Comparator)`、`TreeSet(Collection)`、`TreeSet(SortedSet)`。
- **核心操作**：`add`（重复返 false）、`remove`、`contains`、`size`、`isEmpty`、`clear`、`addAll`。
- **NavigableSet 查询**：`first/last`、`lower/floor/ceiling/higher`、`pollFirst/pollLast`。
- **范围视图**：`subSet/headSet/tailSet`（含 boolean 含边界重载）、`descendingSet`、`descendingIterator`。
- **契约继承**：`clone()`（浅拷贝）、`Serializable`（`serialVersionUID == -2479143000061671589L`）、
  `Set.equals/hashCode`（内容相等，与 comparator 无关）。

## 2. Java 用户如何使用

- `new TreeSet<>()` 自然序；`new TreeSet<>(Comparator)` 自定义序；`new TreeSet<>(otherCollection)` 拷贝去重排序。
- `add` 乱序插入 → `first/last/lower/floor/ceiling/higher` 上下界查询。
- `subSet/headSet/tailSet` 当活视图直接 add/remove/clear（越界抛 IAE），联动到原 set。
- `descendingSet/descendingIterator` 倒序遍历。
- 依赖「null 元素自然序/普通比较器抛 NPE」；`removeIf/removeAll/retainAll` 批量运算。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/TreeSet.html`：NavigableSet 契约、范围视图 write-through、
  null 元素 NPE、clone 浅拷贝、序列化。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/util/TreeSet`（`NullPermitted`、`SubSetClear` 等）。
- **JCK runtime-8b** `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/TreeSet`。

## 4. 测试用例设计（映射到 TreeSetTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 构造器矩阵：null Collection/SortedSet→NPE、null Comparator 等价自然序、TreeSet(Collection) 含 null→NPE、TreeSet(SortedSet) 保留 comparator、null-tolerant comparator 允许 null | testConstructorMatrix |
| P0 | null 语义：add/contains/remove(null) 自然序/普通比较器→NPE | testNullSemantics |
| P0 | NavigableSet 边界：空表 first/last NSEE、poll null、lower/floor/ceiling/higher 越界 null | testNavigableBoundaries |
| P0 | 范围视图写通道：add 越界 IAE、add/remove/clear 联动、from>to IAE、exclusive 边界拒放 | testRangeViewWriteThrough |
| P0 | descendingSet/descendingIterator 活视图：add/remove 联动、倒序、反向 comparator | testDescendingViews |
| P0 | 迭代器写通道：iterator.remove、remove 前 next→ISE、耗尽 next→NSEE、removeAll/retainAll/clear | testIteratorWriteChannels |
| P0 | fail-fast：add/remove/clear 后迭代器/descendingIterator CME | testFailFast |
| P0 | 跨实现 equals：TreeSet vs HashSet 对称 + hashCode；不同 comparator 内容相等 | testCrossImplEquals |
| P1 | toArray 契约：数组复用 ==、null 终止符、精确长度、升序 | testToArrayContract |
| P1 | clone 浅拷贝 + 结构独立 + comparator 保留 | testClone |
| P1 | 序列化 round-trip + serialVersionUID | testSerialization |
| P1 | 状态不变量：删除/失败操作后其余元素完整、clear 后逐元素验证 | testStateInvariants |

## 关键规范细节（易错，已锁入断言）

- `TreeSet(SortedSet)` 保留源 comparator；`TreeSet(Collection)` 自然序且源含 null → NPE。
- `lower/floor/ceiling/higher` 越界返回 null（不是抛异常）；`first/last` 空表抛 NSEE。
- 范围视图 `add` 越界抛 IAE；exclusive 边界同样拒放边界元素。
- 迭代器 `remove` 前未 `next` 抛 ISE；耗尽后 `next` 抛 NSEE。
- null 元素是否合法取决于 comparator（`nullsFirst` 允许；自然序/普通 comparator 抛 NPE）。

## 注意事项

- TreeSet 有序，断言可依赖升序；跨实现 equals 断言只依赖内容相等。
- 序列化 serialVersionUID 锁定官方稳定值（JDK-8 与 JDK-27 相同）。

## 契约深化补充（第二轮：CASE_INSENSITIVE/含边界视图/视图活态+CME/双重 descending/comparator 序列化/nullsFirst/双向 poll/精确特征位/addAll 排序）

新增方法（只追加）：

| 测试点 | 用例方法 |
|---|---|
| String.CASE_INSENSITIVE_ORDER：'A'/'a' 比较==0 视为重复、迭代序 A,b,C、contains/remove 大小写无关 | testCaseInsensitiveComparator |
| headSet(to,true)/tailSet(from,false) 含边界布尔四重载精确端点（last/first/内容清单）+ 视图越界 add IAE | testInclusiveBoundViews |
| 视图活态：父插入范围可见/父删除可见、视图 comparator 同实例、父结构修改后视图迭代器 CME、嵌套子视图写透传 | testViewLivenessAndCme |
| descendingSet().descendingSet() 等价原集且仍为活视图 | testDescendingTwice |
| comparator 序列化：CASE_INSENSITIVE_ORDER 与 nullsFirst 序列化保行为；不可序列化 Comparator → writeObject NotSerializableException | testComparatorSerialization |
| nullsFirst/nullsLast：null 排首/尾、contains(null)、remove(null)、pollFirst 后取最小实元素 | testNullsFirstOrdering |
| pollFirst 升序耗尽/pollLast 降序耗尽/交错首尾剥离 | testPollDrainSequences |
| 中删后仍升序、removeIf 后保序、stream 按序 | testOrderAfterMiddleRemoval |
| spliterator 精确特征位 ORDERED\|DISTINCT\|SORTED\|SIZED（0x55）、getExactSizeIfKnown、getComparator()==comparator()（自然序为 null）、trySplit 两半各自有序且按序拼接==原集 | testSpliteratorDeep |
| hashCode 公式锁 {1,2,3}=6、addAll 乱序输入排序结果、clone 保 comparator 与序 | testHashCodeLockAndAddAllSorting |

负例精确异常表（新锁定）：
- 自然序 add/contains/remove(null) → NPE；nullsFirst 比较器下 null 合法
- 视图越界 add（含 tailSet(from,false).add(from)、headSet(to,true).add(to+1)）→ IllegalArgumentException
- 父集结构修改后视图迭代器 next() → ConcurrentModificationException
- subSet(from>to) → IllegalArgumentException
- 带 Comparator 序列化且 Comparator 不可序列化 → writeObject 抛 NotSerializableException

JDK-8 vs JDK-27：JDK-8 侧无 SequencedSet 方法（getFirst/removeFirst/reversed 在 27 侧断言）；其余契约双侧一致（特征位均 0x55）。
