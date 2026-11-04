# java.util.concurrent.BlockingDeque 设计案

## 1. API 功能
双向阻塞队列接口（继承 BlockingQueue + Deque）。两端 `addFirst/addLast`、`putFirst/putLast`、`takeFirst/takeLast`、带超时 `pollFirst/pollLast`。

## 2. Java 用户如何使用
工作窃取/双端队列场景：可在两端入队出队，`putFirst`/`takeLast` 实现 LIFO 或 FIFO 混合。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.LinkedBlockingDeque` 实现；`test/jdk/java/util/concurrent/BlockingDeque/` 与 tck 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 interface/enum/继承层次 + 反射核对关键方法签名。
2. 黑盒：用具体实现（LinkedBlockingQueue/ConcurrentHashMap/Executor 族等）走多步组合操作并断言可观察行为。

实现：`BlockingDequeTest.java`（`testWhiteBox` + `testBlackBox`）。
