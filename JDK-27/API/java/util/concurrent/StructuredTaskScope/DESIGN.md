# java.util.concurrent.StructuredTaskScope 设计案

## 1. API 功能
结构化并发作用域接口（预览）。`open(Joiner)` 打开、`fork(Callable/Runnable)` 派生子任务、`join()` 等待并聚合、`isCancelled()`/`close()`。

## 2. Java 用户如何使用
try-with-resources 中打开作用域，`fork` 多个子任务，`join()` 按 Joiner 策略聚合结果。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.StructuredTaskScope`（JEP 480 结构化并发，预览）；`test/jdk/java/util/concurrent/structured/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：接口（JDK-27 为接口式 API：open/fork×2/join/isCancelled/close）+ AutoCloseable。
2. 黑盒主干：allSuccessfulOrThrow 两任务成功。
3. 契约深化：默认 open()（join null、子任务默认**虚拟线程**、不取消）（testLifecycle）；裸 open+close 合法、
   close 幂等；非法状态矩阵（join 二次 ISE、fork 后未 join close → ISE "Owner did not join after forking"、
   close 后 fork/join ISE）（testIllegalStates）；Joiner 矩阵（allSuccessfulOrThrow 按提交序、失败 →
   ExecutionException cause 恒等 + 子任务 FAILED/exception 恒等；anySuccessfulOrThrow 混合/全败；awaitAll 两条路径）
   （testJoinerMatrix）；allUntil 谓词触发取消（列表按提交序、被取消子任务 UNAVAILABLE）
   （testAllUntilCancellation）；withTimeout 超时 → ExecutionException(CancelledByTimeoutException)
   （testTimeoutCancellation）；自定义线程工厂 → 平台线程 + 工厂线程名（testThreadConfiguration）；
   fork(Runnable)（get null、SUCCESS）、fork(null) NPE、Subtask 作 Supplier（testForkVariants）。

实现：`StructuredTaskScopeTest.java`（46 断言）。

## 关键规范细节（实测锁定）
- JDK-27 中 STS 仍是 **preview API**（jtreg 需 @enablePreview）。
- join 二次 → ISE "Already joined or scope is closed"；close-before-join → ISE "Owner did not join after forking"。
- 默认子任务为虚拟线程；withThreadFactory 后为平台线程。
- 超时/谓词取消后未完成子任务 state() == UNAVAILABLE（不是 FAILED）。
- lambda 歧义坑：`fork(() -> flag = true)` 会解析成 Callable 返回 Boolean，Runnable 版必须用块体 `{ flag = true; }`。
