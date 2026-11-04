# java.util.AbstractQueue 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。`add` 委托 `offer`（队列满返回 false 时抛 ISE）、`remove` 委托 `poll`
> （空抛 NSEE）、`element` 委托 `peek`（空抛 NSEE）、`clear` 循环 poll、`addAll(null)` NPE、
> `addAll(self)` IAE、`addAll` 批量添加。

## 1. API 提供了哪些功能

- **抽象方法（继承 AbstractCollection）**：`offer`、`poll`、`peek`、`iterator`、`size`。
- **默认实现**：`add`（offer false 时抛 ISE）、`remove`（poll null 时抛 NSEE）、`element`（peek null
  时抛 NSEE）、`clear`（循环 poll）、`addAll`（null NPE、self IAE）。

## 2. Java 用户如何使用

- 自定义队列只需实现 `offer/poll/peek/iterator/size`；`add/remove/element` 自动提供异常语义。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc** `java/util/AbstractQueue.html`。
- **OpenJDK 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/util/AbstractQueue.java`
  （`add` 在 `offer` 返回 false 时抛 IllegalStateException；`remove/element` 空时抛
  NoSuchElementException；`addAll` 先 `requireNonNull`、`if (c == this) throw IAE`）。
- **JCK runtime-8b** `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/AbstractQueue`。

## 4. 测试用例设计（映射到 AbstractQueueTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | add/offer/poll/peek/remove/element 委托 | testAddOfferPoll |
| P0 | 空队列 remove/element 抛 NSEE | testEmptyExceptions |
| P0 | 队列满 add 抛 ISE | testAddWhenFullISE |
| P0 | clear 循环 poll | testClear |
| P0 | addAll(null) NPE / addAll(self) IAE / addAll 批量 | testAddAll |

## 关键规范细节（易错，已锁入断言）

- `add` 委托 `offer`：**offer 返回 false（队列满）时抛 IllegalStateException**。
- `remove` 委托 `poll`、`element` 委托 `peek`：空队列抛 **NoSuchElementException**（peek/poll 返回 null）。
- `clear` 循环 `poll()` 直到返回 null（不抛异常）。
- `addAll(null)` 抛 **NPE**；`addAll(this)` 抛 **IllegalArgumentException**。

## 注意事项

- 无 GC/图形/网络/文件依赖；使用 LinkedList 与自定义有界队列作为实现载体。
