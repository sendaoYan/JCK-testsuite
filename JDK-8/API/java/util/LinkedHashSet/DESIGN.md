# java.util.LinkedHashSet 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。无重复 + 插入序迭代（重复 add 拒绝且不重排）、构造器矩阵（负容量/
> loadFactor≤0/NaN IAE、null Collection NPE、拷贝保留顺序）、null 元素允许、clone 浅拷贝、
> 序列化 round-trip + serialVersionUID、equals/hashCode 顺序无关 + 跨实现（HashSet）、状态不变量。

## 1. API 提供了哪些功能

- **构造器（4）**：`LinkedHashSet()`、`LinkedHashSet(int)`、`LinkedHashSet(int,float)`、
  `LinkedHashSet(Collection)`。
- **核心操作**：`add/remove/contains`（继承 HashSet，元素唯一），保持插入序迭代。
- **契约继承**：extends `HashSet`、implements `Set`/`Cloneable`/`Serializable`
  （`serialVersionUID == -2851667679971038690L`）；`equals/hashCode` 顺序无关（Set 契约）。

## 2. Java 用户如何使用

- `new LinkedHashSet<>()` 建保留插入序的集合 → `add` 去重 → 按插入序遍历 → `remove/contains`。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/LinkedHashSet.html`：插入序、null 允许。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/LinkedHashSet.java`（构造器
  IAE/NPE、基于 LinkedHashMap 实现）。
- **JCK runtime-8b** `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/LinkedHashSet`。

## 4. 测试用例设计（映射到 LinkedHashSetTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 既有：无重复 + 插入序 | testOrderAndDuplicates |
| P0 | 构造器矩阵 | testConstructorMatrix |
| P0 | null 元素允许 | testNullAllowance |
| P1 | clone 浅拷贝 | testClone |
| P1 | 序列化 round-trip + serialVersionUID | testSerialization |
| P1 | 跨实现 equals/hashCode（HashSet） | testCrossImplEquals |
| P1 | 状态不变量 | testStateInvariants |

## 关键规范细节（易错，已锁入断言）

- 迭代顺序 == 插入顺序；重复 `add` 返回 false 且不改变位置；null 元素保持插入位置。
- 构造器：`LinkedHashSet(-1)`、`loadFactor≤0 或 NaN` 抛 IAE；`LinkedHashSet(null Collection)` 抛 NPE；
  `LinkedHashSet(Collection)` 保留源集合的相遇顺序。
- `equals/hashCode` 与顺序无关，与 `HashSet` 内容相同时互相 equals、hashCode 相等。
- `clone` 浅拷贝，与原集合结构独立。

## 注意事项

- 无 GC 依赖、无图形/网络/文件依赖；serialVersionUID 锁定官方稳定值（JDK-8 与 JDK-27 相同）。

## 契约深化补充（第二轮：重插入移尾/写通道/fail-fast/toArray 保序/equals+hashCode 锁/批量返回值/stream+精确特征位/含 null 序列化）

新增方法（只追加）：

| 测试点 | 用例方法 |
|---|---|
| remove+reinsert 把元素移到末尾；重复 add 不重排 | testReinsertMovesToEnd |
| 迭代器写通道：iterator.remove 保序、ISE、空迭代器 NSEE、外部 clear 后 next CME | testIteratorWriteChannels |
| fail-fast：add/remove/clear 后 next 抛 CME；iterator.remove 是唯一合法删除 | testFailFast |
| toArray 契约保序：复用==、精确长度、尾部 null 终止符、插入序内容 | testToArrayOrder |
| hashCode 公式锁（{1,2,3}=6、含 null 97、空 0）+ 跨实现 equals（HashSet/TreeSet）+ Set.equals(List) false | testEqualsHashCodeDeep |
| 批量操作返回值契约（空集/无命中/超集均 false；containsAll(empty) true；null NPE×4） | testBatchOpReturnContracts |
| stream 保 encounter order、spliterator 精确特征位 ORDERED\|DISTINCT\|SIZED\|SUBSIZED（0x4051）、trySplit 前后两半按序拼接==原序列 | testStreamAndSpliterator |
| 含 null 元素序列化保序（null 位置不变）、空集 round-trip 可用 | testSerializationWithNull |

负例精确异常表（新锁定）：
- 迭代中外部 add/remove/clear → 下一次 next() ConcurrentModificationException
- iterator.remove() 未 next（或连续两次）→ IllegalStateException
- 空集迭代器 next() → NoSuchElementException
- addAll/containsAll/removeAll/retainAll(null) → NPE

JDK-8 vs JDK-27：JDK-8 侧无 SequencedSet 方法（addFirst/addLast/getFirst/getLast/removeFirst/removeLast/reversed 为 JDK 21+ API，在 27 侧断言）；其余契约一致。
