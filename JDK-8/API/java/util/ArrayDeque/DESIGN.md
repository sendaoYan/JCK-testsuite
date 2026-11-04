# java.util.ArrayDeque 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。构造器矩阵（负/0 容量可用、null 集合/含 null 集合 NPE）、null 拒绝
> （所有写入口 NPE、contains/remove(null) 返 false）、空 vs 异常语义（poll/peek null、remove/element/
> getFirst/getLast/removeFirst/removeLast/pop NSEE）、push/pop、removeFirst/LastOccurrence、
> 迭代器/descendingIterator、fail-fast（JDK-8 尾部修改触发 CME）、toArray 契约、clone 浅拷贝、
> 序列化 round-trip、环形扩容、状态不变量全部补齐。

## 1. API 提供了哪些功能

- **构造器（3）**：`ArrayDeque()`、`ArrayDeque(int)`、`ArrayDeque(Collection)`。
- **Queue 操作**：`add/offer`、`poll/peek`（空返 null）、`remove/element`（空抛 NSEE）。
- **Deque 操作**：`addFirst/addLast`、`offerFirst/offerLast`、`peekFirst/peekLast`、
  `pollFirst/pollLast`、`getFirst/getLast`、`removeFirst/removeLast`、`push/pop`、
  `removeFirstOccurrence/removeLastOccurrence`、`descendingIterator`。
- **契约继承**：`Cloneable`（clone 浅拷贝）、`Serializable`（`serialVersionUID == 2340985798034038923L`）；
  `equals/hashCode` 未覆写（身份语义，继承 Object）。

## 2. Java 用户如何使用

- `new ArrayDeque<>()` 建队列/栈 → `addLast/addFirst` 两端入队 → `peekFirst/peekLast` 查看 →
  `pollFirst/pollLast` 消费 → `push/pop` 当栈 → `removeFirstOccurrence` 定向删除 → 双向遍历。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/ArrayDeque.html`。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/util/ArrayDeque`（`WhiteBox`、`Grow`、`Null` 等）。
- **JCK runtime-8b** `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/ArrayDeque`。

## 4. 测试用例设计（映射到 ArrayDequeTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 构造器矩阵：null Collection/含 null 集合 NPE、负/0 容量可用、拷贝 | testConstructorMatrix |
| P0 | null 拒绝：所有写入口 NPE、contains/remove(null) 返 false | testNullRejection |
| P0 | 空 vs 异常语义 | testEmptySemantics |
| P0 | push/pop + removeFirst/LastOccurrence | testPushPopAndOccurrences |
| P0 | 迭代器/descendingIterator 顺序 + fail-fast（尾部修改 CME） | testIterators、testFailFast |
| P1 | toArray 契约：复用 ==、null 终止符、精确长度 | testToArrayContract |
| P1 | clone 浅拷贝 + 结构独立 | testClone |
| P1 | 序列化 round-trip + serialVersionUID | testSerialization |
| P1 | 环形扩容 | testGrowth |
| P1 | 状态不变量 | testStateInvariants |

## 关键规范细节（易错，已锁入断言）

- `ArrayDeque(int)` 对负/0 容量**不抛异常**（内部取最小容量 8），仅 null 集合/含 null 集合抛 NPE。
- 禁止 null：所有 `add/offer/addFirst/addLast/offerFirst/offerLast/push` 传 null 抛 NPE；
  `contains(null)/remove(null)` 返回 false（不抛）。
- `ArrayDeque` 继承 `AbstractCollection`，但 **不覆写 equals/hashCode**：内容相等的两个队列
  `equals` 为 false（身份语义）。clone/序列化内容比较须用 `toArray()` 逐项比对。
- 空时：`poll/peek/pollFirst/pollLast/peekFirst/peekLast` 返 null；`remove/element/getFirst/getLast/
  removeFirst/removeLast/pop` 抛 NSEE。
- **fail-fast 弱保证（JDK-8）**：正向迭代器只检测尾部变化（addLast/removeLast 触发 CME），
  头部变化（addFirst/removeFirst）**不**触发 CME；descendingIterator 检测头部变化。

## 注意事项

- 无 GC 依赖、无图形/网络/文件依赖；serialVersionUID 锁定官方稳定值（JDK-8 与 JDK-27 相同）。

## 契约深化追加（第二版）

新增 test 方法：testFailFastMatrix / testEqualsHashCodeIdentity / testToString / testBulkOperations /
testForEachOrder / testSpliterator / testIteratorRemoveSemantics / testWrapAroundSerialization /
testMixedGrowth / testToArrayExactSize / testDuplicateRemoval / testCloneSharesElements /
testAddAllContract。

### 覆盖点

- **fail-fast 完整矩阵（probe 锁定）**：升序迭代器仅在**尾部**修改（addLast/removeLast）后 CME，
  降序迭代器仅在**头部**修改（addFirst/removeFirst）后 CME；对端修改两向均存活
  （栅栏语义：asc 栅 tail、desc 栅 head）。8 格全断言。
- **身份 equals/hashCode**：AbstractCollection 不覆写 equals/hashCode——同内容两个实例不 equals、
  `hashCode() == System.identityHashCode()`、与同内容 List 互不 equals。
- toString 精确锁定："[1, 2, 3]"/"[]"。
- 批量契约：containsAll/removeAll/retainAll/removeIf（顺序保持 + 返回值语义）。
- Spliterator：特征位 **0x4150 = ORDERED|SIZED|SUBSIZED|NONNULL**（getExactSizeIfKnown==size）、
  forEachRemaining 保序、trySplit 前缀+后缀并集==原序、tryAdvance 有/耗尽两态。
- 迭代器 remove 状态机：未 next ISE、双 remove ISE、删当前；descendingIterator.remove 删尾侧已访问元素。
- **环形缓冲序列化**：构造 wrap 状态（加 6 → 头排 4 → 尾加 4）后 round-trip 逻辑序 [4..9] 精确保留。
- 混合增长：300 addFirst + 300 addLast → size 600、头 299/尾 1299、双端排干逐值断言。
- toArray 精确长度数组复用（无终止符写入）；重复元素 remove 只删首现；clone 浅拷贝（元素同实例
  `==`、结构独立）；addAll 追加保序 + 中途 null NPE 时**前面的元素已加入**（部分应用契约）。

### 易错规范细节（已锁入断言）

- equals 是身份语义：**不要**断言跨实例相等或 hashCode 相等。
- addAll 含 null：NPE 在 null 处抛出，之前元素已生效（AbstractCollection 逐个 add）。
