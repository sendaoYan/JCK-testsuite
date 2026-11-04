# java.util.concurrent.TimeoutException 设计案

## 1. API 功能
受检异常（extends Exception）：`Future.get(long, TimeUnit)` 等限时等待在期限内未完成时抛出。构造器仅 `()` 与 `(String)`；无 cause 构造器。与 java.lang 自己的 Timeout 系（如 InterruptedIOException）无继承关系。

## 2. Java 用户如何使用（含开源实际用法调研）
- 典型来源：`future.get(1, SECONDS)` 超时、`BlockingQueue.poll(timeout)` 语义配对、`CyclicBarrier.await(timeout)` 超时（屏障随之 broken）。
- 开源用法：gRPC/JDK http client 的 deadline 适配层常把内部超时转译为 java.util.concurrent.TimeoutException；JUnit `assertTimeout` 的失败包装。
- 实现细节：FutureTask/CompletableFuture 的超时路径抛出时 message 为 null。

## 3. 参考来源
- Oracle javadoc：TimeoutException（"Exception thrown when a blocking operation times out"）。
- OpenJDK：`~/git/jdk8u-dev/jdk/test/java/util/concurrent/FutureTask/`（timed get 超时路径）。
- JCK runtime-8b：`~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/concurrent/TimeoutException/`（含 serial）。

## 4. 测试用例设计 + 易错规范细节
实现：`TimeoutExceptionTest.java`（PASS 26 断言）。
| 场景 | 断言要点 | 优先级 |
|---|---|---|
| 构造器矩阵 | 恰好 2 个 public ctor；getSuperclass()==Exception（受检） | P0 |
| message/toString | 消息保持；两种 toString 形态 | P0 |
| initCause | 返回 this、生效（无 cause ctor，initCause 是唯一通道） | P1 |
| fillInStackTrace | 返回 this；栈非空 | P1 |
| serialVersionUID | == 1900926677490660714（实测锁定） | P0 |
| 序列化 round-trip | 消息/null cause/toString 保持 | P0 |
| 集成来源 | 未完成 future 的 timed get 抛本类型且 message == null | P0 |
| 易错细节 | 超时不改变 future 状态（isDone 仍 false）——别断言完成 | P1 |
