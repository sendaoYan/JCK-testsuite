# java.util.concurrent.CompletionService 设计案

## 1. API 功能
完成服务接口：把已完成任务与提交解耦。`submit(Callable)`/`submit(Runnable,V)` 提交；`take()`/`poll()`/`poll(timeout)` 取已完成 Future。

## 2. Java 用户如何使用
并行提交一批任务，按完成顺序 `take()` 消费结果（而非按提交顺序）。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.ExecutorCompletionService` 实现；`test/jdk/java/util/concurrent/ExecutorCompletionService/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景（深化批次追加接口结构锁 + Runnable 固定结果形态）：
1. 白盒：`Class.forName` 断言 interface/enum/继承层次 + 反射核对关键方法签名。
2. 黑盒：用具体实现（ExecutorCompletionService）走完成序/poll(timeout) 组合操作并断言可观察行为。
3. 接口结构锁：单类型参数 V；恰好 5 个声明方法（锁定方法面）；5 方法全部 abstract（无 default）；ExecutorCompletionService 实现本接口；与 ExecutorService 互不 assignable。
4. Runnable+固定结果形态：`submit(Runnable, 7)` 的 Future.get()==7；take() 取回的是 submit 时返回的同一 Future 对象（队列可见性）。

实现：`CompletionServiceTest.java`（`testWhiteBox` + `testBlackBox` + `testInterfaceLock` + `testRunnableForm`，PASS 25 断言）。
