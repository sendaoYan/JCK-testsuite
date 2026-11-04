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

## 契约深化追加（第二版）

新增 test 方法：testSixMethodTwoStateTable / testFifoDiscipline / testCrossCarrierNullSemantics /
testRemoveObjectSemantics / testInterfaceShape。

### 1. API 功能（补充）

- 六方法两态表形式化：add/offer/remove/poll/element/peek ×（空/非空）×（返回值/异常/size 副作用）。
- 空队列 poll/peek 反复调用稳定返回 null（不抛异常、不翻脸）。
- FIFO 纪律：交错 offer/poll 仍严格先进先出。

### 2. 使用方式（补充）

- 双载体对照：ArrayDeque（拒 null）与 LinkedList（容 null）同为 Queue——
  `offer(null)` 在前者 NPE、后者成功且 `contains(null)` 为 true（**null 返回值歧义**：poll 到 null 可能是空也可能是 null 元素）。

### 3. 参考来源（补充）

- Oracle javadoc Queue.html（两态表规范）；OpenJDK ArrayDeque/LinkedList 源码行为实测。

### 4. 测试用例设计（补充）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 六方法两态表（含空态稳定 null） | testSixMethodTwoStateTable |
| P0 | FIFO 纪律 + 交错操作 | testFifoDiscipline |
| P0 | 跨实现 null 语义差异（ArrayDeque vs LinkedList） | testCrossCarrierNullSemantics |
| P1 | remove(Object) 头/中/尾/缺失/重复首现 | testRemoveObjectSemantics |
| P1 | 接口形状：Queue 无 default 方法（六方法全抽象） | testInterfaceShape |

### 易错规范细节（已锁入断言）

- Queue 接口本身**不提供任何 default 实现**（六方法全抽象；分派实现来自 AbstractQueue 等骨架）。
- LinkedList 作 Queue 时 `poll()==null` 有歧义（空 vs null 元素），测试显式区分两态。
