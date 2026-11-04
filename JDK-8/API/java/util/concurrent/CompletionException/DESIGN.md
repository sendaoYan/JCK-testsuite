# java.util.concurrent.CompletionException 设计案

## 1. API 功能
非受检异常（extends RuntimeException）：`CompletableFuture.join()`（及 `CompletableFuture` 树内部）把任务异常原样包装后抛出的载体（对照受检的 ExecutionException）。构造器仅 `(String, Throwable)` 与 `(Throwable)`，**无无参构造器**。

## 2. Java 用户如何使用（含开源实际用法调研）
- 典型来源：`cf.join()`；`thenApply` 链上未消费的异常在 join 时以本类型爆出（cause 为原始异常）。
- 开源用法：Spring WebFlux 适配 CompletableFuture 时把 join 的 CompletionException 解包上报；ForkJoin 任务内部用它包装非受检失败。
- 用户契约关注点：cause 不可丢（getCause 恒等原始对象）、`((Throwable)null)` 造的实例 message 为 null。

## 3. 参考来源
- Oracle javadoc：CompletionException（"a RuntimeException counterpart of ExecutionException"、构造器语义）。
- OpenJDK：`~/git/jdk8u-dev/jdk/test/java/util/concurrent/CompletableFuture/`（join 包装路径）。
- JCK runtime-8b：`~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/concurrent/CompletionException/`（含 serial）。

## 4. 测试用例设计 + 易错规范细节
实现：`CompletionExceptionTest.java`（PASS 32 断言）。
| 场景 | 断言要点 | 优先级 |
|---|---|---|
| 构造器矩阵 | 恰好 2 个 public ctor；**无 ()**；getSuperclass()==RuntimeException | P0 |
| (Throwable) 语义 | cause 恒等；message == cause.toString()（Throwable(Throwable) 契约） | P0 |
| ((Throwable)null) | getCause()==null 且 getMessage()==null | P0 |
| (String,null) | message 保持字符串、cause 为 null（非 this） | P0 |
| (String,Throwable) | message 是字符串本身，不是 cause.toString() | P0 |
| initCause | ctor 已给 cause 后再 initCause → IllegalStateException | P1 |
| serialVersionUID | == 7830266012832686185（与 ExecutionException 同值，实测锁定） | P0 |
| 序列化 round-trip | 消息/cause（类型+消息）/null-cause 保持 | P0 |
| 集成来源 | completeExceptionally 后 join 抛本类型且 cause 是原始异常 | P0 |
| 易错细节 | toString == "类名: " + cause.toString()（因 message 即 cause.toString()）；`new CompletionException()` 编译不过 | P1 |
