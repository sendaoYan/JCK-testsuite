# java.util.concurrent.ForkJoinTask 设计案（JDK-8）

## 1. API 功能
抽象任务基类，实现 `Future`/`Serializable`。核心 `fork()`/`join()`/`invoke()`/`invokeAll`(2参/数组/集合)；完成控制 `complete(V)`/`completeExceptionally`/`quietlyComplete`/`reinitialize`；状态 `isDone`/`isCancelled`/`isCompletedNormally`/`isCompletedAbnormally`/`getException`；取值 `get()`/`get(timeout)`/`getRawResult`；静默版 `quietlyJoin`/`quietlyInvoke`；池内探针 `getPool`/`inForkJoinPool`/`tryUnfork`/`getQueuedTaskCount`/`getSurplusQueuedTaskCount`/`helpQuiesce`；**tag 方法（getForkJoinTaskTag/setForkJoinTaskTag/compareAndSetForkJoinTaskTag，JDK-8 即有，short 类型 CAS）**；适配 `adapt`(Runnable/Runnable,T/Callable)。

## 2. Java 用户如何使用
- 通常不直接继承，而用 `RecursiveTask`/`RecursiveAction`/`CountedCompleter`。
- compute 内惯用法：`left.fork(); return left.join() + right.compute();` 或 `invokeAll(left, right)`。
- 主线程可 `task.invoke()` 立即执行（caller-runs）；`task.fork()` 提交到 commonPool。
- `complete(V)` 预注入结果则 exec() 永不执行；`reinitialize()` 清状态复用（rawResult 保留，文档 "unaffected"）。

## 3. 开源代码实际用法调研
- JDK 自用：`CompletableFuture.AsyncSupply`、`java.util.stream.AbstractTask` 继承 CountedCompleter；parallel sort 内部 fork/join。
- JDK 自带测试：`tck/ForkJoinTaskTest.java` 覆盖取消/异常/tag/adapt 矩阵。

## 4. 测试用例设计 + 自动化实现
场景（126 断言）：
1. 白盒：abstract/非接口/Future/Serializable + 33 个方法名逐一存在性（含 tag 与 adapt 三形式）。
2. 主线程 invoke()：返回 42、isCompletedNormally、get/getRawResult 一致。
3. fork/join 配对：主线程 inForkJoinPool=false、getPool=null；fork+join=21；未 fork 任务 tryUnfork=false。
4. 池内身份：inForkJoinPool true、getPool==pool、worker 线程、surplus/queued 非负、helpQuiesce。
5. invokeAll 形式：2 参/数组/集合/空变体（varargs/数组/空集合）全部无异常；元素失败 rethrow 未检异常类。
6. 嵌套 invokeAll：Fib(10)==55 无死锁。
7. 异常同线程：invoke/join/getException 三处 **== 原对象**；get() → EE cause==原对象。
8. 异常跨线程：重建副本 != 原对象、cause==原对象；quietlyInvoke/quietlyJoin 不抛。
9. 取消：fresh cancel true → isCancelled、get/join → CancellationException；**重复 cancel 持续返回 true（FJT 语义：true iff cancelled upon return，与常见 Future 不同）**；in-flight cancel。
10. get(150ms) → TimeoutException。
11. complete(99) 注入：exec 计数 0、invoke/get/join 全部返回 99。
12. completeExceptionally：join == 注入对象、EE cause==注入对象。
13. reinitialize：两次执行、异常状态清空、isDone 复位。
14. tag：初值 0、set 返回旧值、CAS 成败。
15. adapt 三形式：Runnable null / Runnable,T / Callable 值；Callable 异常同线程 == 原对象。

易错规范细节：异常对象身份断言仅同线程；跨线程只断言 class+getCause()==original；不做时序断言；被阻塞任务全部用 latch 且 finally 释放。

实现：`ForkJoinTaskTest.java`（126 断言）。优先级：异常/取消/完成注入契约 > invokeAll 形式 > tag/adapt。
