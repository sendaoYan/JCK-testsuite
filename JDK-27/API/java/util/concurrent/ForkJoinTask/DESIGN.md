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
场景（166 断言）：
1. 白盒：抽象类 + Future/Serializable + 41 个方法名存在性（含 JDK-19+ state/resultNow/exceptionNow/quietlyJoin(long,TimeUnit)/quietlyJoinUninterruptibly 与 JDK-20+ adaptInterruptible×3）。
2. 黑盒：invoke 取原始结果、fork/join 递归 fib(10)==55、adapt(Runnable,V)/adapt(Callable)、complete(V)/completeExceptionally/getException。
3. invoke 同线程调用（caller-runs）；fork/join 配对；tryUnfork false；池内身份探针（inForkJoinPool/getPool/surplus/queued 非负/helpQuiesce）。
4. invokeAll 全形式（2 参/数组/集合/空变体）+ 失败 rethrow。
5. 异常语义：同线程 invoke/join/getException **== 原对象**、get → EE cause==原对象；跨线程 rebuilt+cause；quietlyInvoke/quietlyJoin 不抛。
6. 取消：fresh/in-flight cancel、get/join → CancellationException、**重复 cancel 持续 true**。
7. get(150ms) → TimeoutException；complete(99) 注入（exec 计 0）；completeExceptionally 注入。
8. reinitialize：两次执行、异常清空；tag 全矩阵（初值 0/set 返旧值/CAS 成败）；adapt 三形式。
9. JDK-19+ 状态机矩阵：fresh RUNNING、成功 SUCCESS+resultNow、失败 FAILED+exceptionNow==原对象、取消 CANCELLED；resultNow 在 RUNNING/FAILED/CANCELLED → ISE；exceptionNow 在 RUNNING/SUCCESS/CANCELLED → ISE（**FJT 取消不记录异常对象，exceptionNow 抛 ISE 而非 Future 默认建议的 CancellationException**）。
10. timed quietlyJoin：完成 true；未启动 false（等待超时本身有界）；quietlyJoinUninterruptibly 同。
11. adaptInterruptible×3：Runnable null / Runnable,T / Callable 值；Callable 异常同线程 == 原对象。

易错规范细节：异常 == 断言仅同线程；**quietlyJoin(timeout) 对 fork 后未完成任务会由调用线程自跑（帮助执行），超时参数失效，绝不可用于时序断言**；阻塞任务 latch 且 finally 释放。

实现：`ForkJoinTaskTest.java`（166 断言）。优先级：状态机/异常/取消契约 > invokeAll 形式 > tag/adapt/adaptInterruptible。
