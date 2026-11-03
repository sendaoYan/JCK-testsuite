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
