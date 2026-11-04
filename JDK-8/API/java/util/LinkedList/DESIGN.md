# java.util.LinkedList 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。构造器矩阵、null 元素容忍、Queue/Deque 空 vs 异常语义、Deque 变体、
> descendingIterator、listIterator（add/set/remove/previous/nextIndex/ISE）、fail-fast（CME）、
> toArray 契约、subList 活视图、跨实现 equals（vs ArrayList）、clone 浅拷贝、序列化 round-trip、
> 状态不变量全部补齐。

## 1. API 提供了哪些功能

- **构造器（2）**：`LinkedList()`、`LinkedList(Collection)`。
- **List 操作**：`add`（尾/索引）、`get`、`set`（返旧值）、`remove`（索引/对象）、`indexOf/lastIndexOf`、
  `contains`、`addAll`、`subList`。
- **Queue 操作**：`offer/poll/peek`（空返 null）vs `add/remove/element`（空抛 NSEE）。
- **Deque 操作**：`addFirst/addLast`、`getFirst/getLast`、`removeFirst/removeLast`、
  `offerFirst/offerLast`、`peekFirst/peekLast`、`pollFirst/pollLast`、
  `removeFirstOccurrence/removeLastOccurrence`、`push/pop`、`descendingIterator`。
- **契约继承**：`Cloneable`（clone 浅拷贝）、`Serializable`（`serialVersionUID == 876323262645176354L`）、
  `List.equals/hashCode`。

## 2. Java 用户如何使用

- `new LinkedList<>()` 建表 → `add/addFirst/addLast/offer` 填充（含 null）→ `get/set/remove` 改表 →
  队列 poll/peek 消费、push/pop 当栈、descendingIterator 反向遍历、listIterator 双向遍历。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/LinkedList.html`。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/util/LinkedList`（`AddAll`、`Remove`、`ComodifiedRemove` 等）。
- **JCK runtime-8b** `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/LinkedList`。

## 4. 测试用例设计（映射到 LinkedListTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 构造器矩阵：null Collection→NPE、拷贝、默认空 | testConstructorMatrix |
| P0 | null 容忍：add/offer/addFirst 接受 null、contains/indexOf/remove/poll null | testNullTolerance |
| P0 | Queue/Deque 空 vs 异常语义：poll/peek null、remove/element/getFirst/... NSEE | testQueueDequeEmptySemantics |
| P0 | Deque 变体：offerFirst/Last、peek、poll、removeFirst/LastOccurrence | testDequeVariants |
| P0 | descendingIterator 反向遍历 | testDescendingIterator |
| P0 | listIterator：add/set/remove/previous/nextIndex/previousIndex + set/remove 前未 next ISE | testListIteratorContract |
| P0 | fail-fast：iterator/listIterator/descendingIterator CME | testFailFast |
| P0 | 跨实现 equals：LinkedList vs ArrayList 对称 + hashCode | testCrossImplEquals |
| P1 | toArray 契约：复用 ==、null 终止符、精确长度 | testToArrayContract |
| P1 | subList 活视图 + IOOBE | testSubListView |
| P1 | clone 浅拷贝 + 结构独立 | testClone |
| P1 | 序列化 round-trip（含 null）+ serialVersionUID | testSerialization |
| P1 | 状态不变量：删除/失败操作后其余元素完整、clear 后逐元素验证 | testStateInvariants |

## 关键规范细节（易错，已锁入断言）

- `addFirst/addLast` 返回 void，不能用作布尔表达式。
- 队列空：`poll/peek` 返 null、`remove/element` 抛 NSEE；`pop` 空抛 NSEE。
- listIterator `set/remove` 前未 `next/previous` 抛 ISE。
- `toArray(T[])` 容量足够复用同一数组、多余位补 null。

## 注意事项

- 无 GC 依赖、无图形/网络/文件依赖；serialVersionUID 锁定官方稳定值（JDK-8 与 JDK-27 相同）。
