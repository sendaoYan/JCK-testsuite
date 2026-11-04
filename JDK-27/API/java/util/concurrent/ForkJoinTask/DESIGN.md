# java.util.concurrent.ForkJoinTask 设计案

## 1. API 功能
抽象类，实现 `Future<V>` 与 `Serializable`。抽象方法 `getRawResult`/`setRawResult`/`exec`。操作 `fork()`/`join()`/`invoke()`/`invokeAll`；`cancel`/`isDone`/`isCancelled`/`isCompletedNormally`/`isCompletedAbnormally`/`get`/`getException`；`complete`/`completeExceptionally`/`quietlyComplete`；适配 `adapt(Runnable[,V])`/`adapt(Callable)`。

## 2. Java 用户如何使用
- 子类化：实现 `exec()` 返回 true 并 `setRawResult`；或经 `RecursiveTask`/`RecursiveAction`。
- 链路：`a.fork()` 后 `b.compute()` 再 `a.join()` 分治；`pool.invoke(task)` 取原始结果；`complete(V)`/`completeExceptionally(t)` 显式完成。

## 3. 开源代码实际用法调研
- JDK 自用：`RecursiveTask`/`RecursiveAction`/`CountedCompleter` 均继承 ForkJoinTask；`ForkJoinPool` 调度其执行。
- JDK 自带测试：`tck/ForkJoinTaskTest.java`、`tck/CountedCompleterTest.java`。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：抽象类 + 实现 Future/Serializable + fork/join/invoke/cancel/get/complete/completeExceptionally/getException/isCompletedAbnormally/adapt。
2. 黑盒：invoke 取原始结果、fork/join 递归斐波那契、adapt(Runnable,V)/adapt(Callable)、complete(V)/completeExceptionally/getException/isCompletedAbnormally。

实现：`ForkJoinTaskTest.java`（23 断言）。
