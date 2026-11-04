# java.util.concurrent.CancellationException 设计案

## 1. API 功能
非受检异常（extends IllegalStateException）：任务已被取消时 `Future.get()` 抛出。构造器仅 `()` 与 `(String)`；无 cause 构造器。与 `IllegalStateException` 的继承关系是其契约重点（允许现有 catch(ISE) 兼容旧版 FutureTask 行为）。

## 2. Java 用户如何使用（含开源实际用法调研）
- 典型来源：`FutureTask.cancel` 之后 `get()`、`CompletableFuture.cancel` 之后 `join/get`。
- 框架用法：Spring `@Async` 取消传播、Guava `Futures.canceled()`、Netflix Hystrix 命令取消时把结果包装为本异常抛给调用方。
- 用户常以 `instanceof IllegalStateException` 做兜底捕获，故父类精确性是兼容性契约。

## 3. 参考来源
- Oracle javadoc：CancellationException（extends IllegalStateException 说明）。
- OpenJDK：`~/git/jdk8u-dev/jdk/test/java/util/concurrent/FutureTask/`（cancel 后 get 的取消路径）。
- JCK runtime-8b：`~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/concurrent/CancellationException/`（含 serial）。

## 4. 测试用例设计 + 易错规范细节
实现：`CancellationExceptionTest.java`（PASS 27 断言）。
| 场景 | 断言要点 | 优先级 |
|---|---|---|
| 构造器矩阵 | 恰好 2 个 public ctor；getSuperclass()==IllegalStateException 精确；属 RuntimeException 族 | P0 |
| message/toString | 消息精确保持；toString 两种形态（有/无消息） | P0 |
| initCause | 返回 this、生效（本类无 cause ctor，initCause 是唯一通道） | P1 |
| fillInStackTrace | 返回 this；栈非空 | P1 |
| serialVersionUID | == -9202173006928992231（实测锁定） | P0 |
| 序列化 round-trip | 消息/null cause/toString 保持 | P0 |
| 集成来源 | FutureTask cancel 后 get 抛本异常且 message == null（FutureTask 不带消息） | P0 |
| 易错细节 | 未来任务取消路径抛出时 message 为 null——不要断言含 "cancelled" 字样 | P1 |
