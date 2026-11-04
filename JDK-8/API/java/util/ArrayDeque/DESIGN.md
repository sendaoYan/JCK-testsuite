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
