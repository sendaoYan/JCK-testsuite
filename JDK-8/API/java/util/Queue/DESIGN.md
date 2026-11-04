# java.util.Queue 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。offer/add 返回语义、peek/element 只读头、poll/remove 移除头、
> 空队列 peek/poll 返回 null 而 element/remove 抛 NSEE、null 参数 NPE。

## 1. API 提供了哪些功能

- **两类入队**：`offer`（返回 false 表示容量满）与 `add`（满时抛 ISE）。
- **两类查头**：`peek`（空返回 null）与 `element`（空抛 NSEE）。
- **两类出队**：`poll`（空返回 null）与 `remove`（空抛 NSEE）。

## 2. Java 用户如何使用

- 无界队列（ArrayDeque）下 `offer`/`add` 均成功；有界队列用 `offer` 探测容量、
  `poll` 探测是否为空。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/Queue.html`。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/ArrayDeque.java`（null 拒绝）。

## 4. 测试用例设计（映射到 QueueTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | offer/add + null NPE | testOfferAdd |
| P0 | peek/element 只读头 | testPeekElement |
| P0 | poll/remove 移除头 | testPollRemove |
| P0 | 空队列 null vs NSEE | testEmptySemantics |
| P0 | Collection 契约（contains/remove/iterator） | testCollectionContract |

## 关键规范细节（易错，已锁入断言）

- 空队列：`peek`/`poll` 返回 **null**，`element`/`remove` 抛 **NoSuchElementException**。
- ArrayDeque 拒绝 null 元素：`offer(null)`/`add(null)` 抛 **NPE**。

## 注意事项

- 用 ArrayDeque（无界、拒 null）为载体；无 GC/图形/网络依赖。
