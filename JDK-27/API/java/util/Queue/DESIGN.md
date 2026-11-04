# java.util.Queue — 测试设计案

## 1. API 功能
FIFO 队列接口。继承 Collection。

- 抽象：add/offer（入队）、remove/poll（出队）、element/peek（查看队首）。

## 2. Java 用户如何使用
`Queue<E> q = new ArrayDeque<>();` offer/add 入队、peek/element 查看、poll/remove 出队；
空队列 peek/poll 返回 null、element/remove 抛 NSEE。
链路：offer/add -> peek/element -> poll/remove -> 空队列语义。

## 3. 开源代码实际用法调研
- OpenJDK `ArrayDeque`（本地 `~/git/jdk25u-dev`）实现全部抽象方法；空队列 element/remove 抛
  NoSuchElementException、peek/poll 返回 null——第 1/2/4 节对齐。
- 官方测试 `test/jdk/java/util/Queue/`.

## 4. 测试用例设计 + 自动化实现（QueueTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 入队 | offer/add 顺序 |
| 查看 | peek/element 不删除 |
| 出队 | poll/remove 删除并返回 |
| 空队列 | peek/poll 返回 null、element/remove 抛 NSEE |

共 4 组场景、约 10 个 check（`testBlackBox()`）。

## 契约深化追加（第二版）

新增 test 方法：testSixMethodTwoStateTable / testFifoDiscipline / testCrossCarrierNullSemantics /
testRemoveObjectSemantics / testInterfaceShape。

### 覆盖点

- 六方法两态表（add/offer/remove/poll/element/peek × 空/非空）：返回值、异常、size 副作用逐格断言；
  空态 poll/peek 反复调用稳定 null。
- FIFO 纪律：交错 offer/poll 仍先进先出；排干后再入队可用。
- 跨载体 null 语义：ArrayDeque 拒 null（NPE）vs LinkedList 容 null（contains(null) true；
  poll 返回 null 有歧义——空 vs null 元素，分两态断言）。
- remove(Object)：头/中/尾/缺失 false/重复只删首现；containsAll 子集判定。
- 接口形状：Queue 仍**无 default 方法**；**JDK-21+ Queue 未加入 SequencedCollection**
  （isAssignableFrom 为 false——与 List/Deque 不同，锁入断言）。

### 易错规范细节

- `element()`/`remove()` 空态抛 NoSuchElementException，`peek()`/`poll()` 空态返回 null。
