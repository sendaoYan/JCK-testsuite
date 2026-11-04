# java.util.TreeSet 测试设计案（JDK-27）

> 覆盖目标：**完整契约**，白盒（反射结构）+ 黑盒（行为契约）。构造器矩阵、null 元素语义、
> NavigableSet 边界、范围视图活对象写通道、倒序视图、迭代器写通道 + fail-fast、toArray 契约、
> 跨实现 equals、clone/序列化、状态不变量全部补齐；外加 JDK 21+ SequencedSet 特有 `addFirst/addLast`
> （有序 Set 恒抛 UOE）、`reversed()`。

## 1. API 提供了哪些功能

- **构造器（4）**：`TreeSet()`、`TreeSet(Comparator)`、`TreeSet(Collection)`、`TreeSet(SortedSet)`。
- **核心操作**：`add/remove/contains/size/isEmpty/clear/addAll`。
- **NavigableSet 查询**：`first/last`、`lower/floor/ceiling/higher`、`pollFirst/pollLast`。
- **范围视图**：`subSet/headSet/tailSet`、`descendingSet`、`descendingIterator`。
- **JDK 21+ SequencedSet**：`addFirst/addLast`（有序 Set 对 TreeSet 恒抛 UOE）、`reversed()`。
- **契约继承**：`clone()`（浅拷贝）、`Serializable`（`serialVersionUID == -2479143000061671589L`）。

## 2. Java 用户如何使用

- 与 JDK-8 一致：自然序/Comparator 建表、NavigableSet 上下界查询、范围/倒序视图直接改、
  null 元素抛 NPE、批量运算。

## 3. 参考来源（不凭记忆编造）

- **Oracle javadoc** `java/util/TreeSet.html`（JDK-8 与 JDK-27）。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/util/TreeSet` + `~/git/jdk/test/jdk/java/util/TreeSet`。
- **JCK runtime-8b** `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/TreeSet`。

## 4. 测试用例设计（映射到 TreeSetTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒结构：继承 AbstractSet、实现 NavigableSet/Cloneable/Serializable、成员形态 | testWhiteBox |
| P0 | 构造器矩阵 + null 语义 | testConstructorMatrix、testNullSemantics |
| P0 | NavigableSet 边界 + 范围视图写通道 + 倒序视图 | testNavigableBoundaries、testRangeViewWriteThrough、testDescendingViews |
| P0 | 迭代器写通道 + fail-fast | testIteratorWriteChannels、testFailFast |
| P0 | 跨实现 equals/hashCode | testCrossImplEquals |
| P1 | toArray 契约、clone、序列化、状态不变量 | testToArrayContract、testClone、testSerialization、testStateInvariants |
| P1 | JDK 21+ SequencedSet：addFirst/addLast → UOE | testBlackBox（已含） |

## 关键规范细节（易错，已锁入断言）

- 同 JDK-8 版；额外：JDK 21+ `addFirst/addLast` 对 TreeSet 恒抛 `UnsupportedOperationException`
  （有序 Set 由排序决定位置）。

## 注意事项

- 白盒反射只断言公开结构；serialVersionUID 与 JDK-8 相同。

## 契约深化补充（第二轮：CASE_INSENSITIVE/含边界视图/视图活态+CME/双重 descending/comparator 序列化/nullsFirst/双向 poll/精确特征位/addAll 排序/Sequenced 方法）

新增方法（只追加）：

| 测试点 | 用例方法 |
|---|---|
| String.CASE_INSENSITIVE_ORDER：'A'/'a' 重复拒绝、迭代序、大小写无关 contains/remove | testCaseInsensitiveComparator |
| 含边界布尔四重载视图精确端点 + 越界 add IAE | testInclusiveBoundViews |
| 视图活态（父增删可见、comparator 同实例、父修改后视图迭代器 CME、嵌套视图写透传） | testViewLivenessAndCme |
| descendingSet().descendingSet() 等价且活 | testDescendingTwice |
| comparator 序列化（CASE_INSENSITIVE/nullsFirst 保行为；不可序列化 → NotSerializableException） | testComparatorSerialization |
| nullsFirst/nullsFirst 全行为 | testNullsFirstOrdering |
| pollFirst/pollLast 耗尽与交错剥离 | testPollDrainSequences |
| 中删/ removeIf / stream 保序 | testOrderAfterMiddleRemoval |
| spliterator 精确特征位 0x55 + getComparator 契约 + trySplit 有序拼接 | testSpliteratorDeep |
| hashCode 锁 + addAll 排序 + clone 保 comparator | testHashCodeLockAndAddAllSorting |
| JDK 21+ SequencedSet：getFirst/getLast==first/last、removeFirst/removeLast==poll 语义、reversed()==descendingSet 内容、空集四方法 NSEE（addFirst/addLast UOE 已在 testBlackBox 断言） | testSequencedMethods |

负例精确异常表（新锁定）：
- 自然序 add/contains/remove(null) → NPE；nullsFirst 比较器下 null 合法
- 视图越界 add → IllegalArgumentException；父修改后视图迭代器 → CME；subSet(from>to) → IAE
- TreeSet.addFirst/addLast → UnsupportedOperationException（排序集不可插首位）
- 空集 getFirst/getLast/removeFirst/removeLast → NoSuchElementException
- Comparator 不可序列化 → writeObject NotSerializableException

JDK-8 vs JDK-27 差异：JDK 21+ TreeSet 继承 SequencedSet 方法（addFirst/addLast 恒 UOE、getFirst/removeFirst/reversed 可用）——27 侧断言、8 侧无此 API；其余契约双侧一致。
