# java.util.concurrent.CompletionService 设计案

## 1. API 功能
完成服务接口：把已完成任务与提交解耦。`submit(Callable)`/`submit(Runnable,V)` 提交；`take()`/`poll()`/`poll(timeout)` 取已完成 Future。

## 2. Java 用户如何使用
并行提交一批任务，按完成顺序 `take()` 消费结果（而非按提交顺序）。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.ExecutorCompletionService` 实现；`test/jdk/java/util/concurrent/ExecutorCompletionService/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 interface/enum/继承层次 + 反射核对关键方法签名。
2. 黑盒：用具体实现（LinkedBlockingQueue/ConcurrentHashMap/Executor 族等）走多步组合操作并断言可观察行为。

实现：`CompletionServiceTest.java`（`testWhiteBox` + `testBlackBox`）。
