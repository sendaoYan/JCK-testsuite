# java.util.Deque — 测试设计案

## 1. API 功能
双端队列接口：`extends Queue, SequencedCollection`，两端都可插入/删除/检查。

- 头操作：addFirst/offerFirst/removeFirst/pollFirst/getFirst/peekFirst。
- 尾操作：addLast/offerLast/removeLast/pollLast/getLast/peekLast。
- 栈语义：push（=addFirst）/pop（=removeFirst）。
- 对象删除：removeFirstOccurrence/removeLastOccurrence。
- 遍历：descendingIterator（逆序）、reversed()（default 视图）。

## 2. Java 用户如何使用
作队列用 offer/poll；作栈用 push/pop；两端调度用 addFirst/addLast+removeFirst/removeLast。
链路：addFirst/addLast -> getFirst/getLast -> pollFirst/pollLast -> push/pop -> removeFirstOccurrence
-> descendingIterator -> reversed。

## 3. 开源代码实际用法调研
- OpenJDK `src/java.base/share/classes/java/util/Deque.java`（本地 `~/git/jdk25u-dev`）：
  push/pop 分别委托 addFirst/removeFirst；reversed() 是 default——第 1/3/6 节对齐。
- OpenJDK `src/java.base/share/classes/java/util/ArrayDeque.java` 实现 Deque。
- OpenJDK `test/jdk/java/util/Deque/*`：官方测试覆盖 ArrayDeque/LinkedList 的 Deque 契约。

## 4. 测试用例设计 + 自动化实现（DequeTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 两端增查 | addFirst/addLast/getFirst/getLast/peek/offer |
| 两端删 | removeFirst/removeLast/pollFirst/pollLast；空队列 poll null、remove/get 抛 NSEE |
| push/pop | LIFO |
| 出现删除 | removeFirstOccurrence/removeLastOccurrence |
| descendingIterator | 逆序 |
| reversed | 头尾互换视图 |
| Collection 契约 | contains/size/remove(Object) |

共 7 组场景、约 20 个 check（`testBlackBox()`）。
