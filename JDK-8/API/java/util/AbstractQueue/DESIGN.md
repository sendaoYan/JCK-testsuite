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

## 契约深化追加（第二版）

新增 test 方法：testDispatchCounters / testAddFullExactMessage / testClearPollLoop /
testAddAllSemantics / testAddAllPartialFailure / testInheritedCollectionOps /
testEqualsHashCodeIdentity / testWhiteBoxStructure。

### 覆盖点

- **分派计数器探针**（Counting 子类）：add→恰一次 offer；element→恰一次 peek；remove→恰一次 poll；
  逐次调用计数 1:1 累计证明默认实现路径。
- **add 满抛 ISE 且 message 恰为 "Queue full"**（精确消息锁定）。
- **clear 是 poll 循环**：2 个元素 clear 恰好 poll 3 次（n 个元素 + 1 次返回 null 的哨兵探测）。
- addAll 语义：空集合 false 且零次 offer；正常路径逐元素经 add（offer 计数==元素数）；
  addAll(self) IAE；**AbstractQueue.addAll 自身不预检 null 元素**——NPE 由底层队列的 add/offer
  中继抛出，且**null 之前的元素已完成入队（部分应用）**（LinkedList 载体不抛、ArrayDeque 载体
  NPE+部分入队，双探针对照锁定）。
- 有界队列 addAll 部分失败：ISE 后先前元素保留（size==1）。
- 继承自 AbstractCollection 的 contains/remove(Object)/containsAll/toString "[m, n]"/"[]"。
- 身份 equals/hashCode（不覆写）。
- 白盒：add/remove/element/clear/addAll 恰为 AbstractQueue 声明且非抽象的五个公共方法。

### 易错规范细节（已锁入断言）

- javadoc 说 addAll 对 null 元素 NPE，实际是**委托给 add 的中继**——用 null 容忍/拒绝双载体对照锁定。
- "Queue full" 是规范规定的消息文本，非实现细节。
