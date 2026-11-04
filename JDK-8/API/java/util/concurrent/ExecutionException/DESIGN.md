# java.util.concurrent.ExecutionException 设计案

## 1. API 功能
受检异常（extends Exception）：`Future.get()` / `invokeAll` 结果把任务内抛出的异常包装后上抛。构造器仅 `(String, Throwable)` 与 `(Throwable)`，**无无参构造器**。

## 2. Java 用户如何使用（含开源实际用法调研）
- 典型来源：`future.get()` 捕获后 `e.getCause()` 拿业务异常（几乎所有线程池使用者都写这个样板）。
- 开源用法：Guava `Futures.getUnchecked` 解包本异常；Spring `ThreadPoolTaskExecutor.submit` 的调用方约定；JUnit `assertThrows` 常见目标。
- 关键契约：cause 必须可恢复；`new ExecutionException("m", null)` 的 cause 是 null（不是 this 也不是默认值）。

## 3. 参考来源
- Oracle javadoc：ExecutionException（构造器 + "the cause cannot be represented by ..." 说明）。
- OpenJDK：`~/git/jdk8u-dev/jdk/test/java/util/concurrent/FutureTask/`、`tck/FutureTaskTest.java`。
- JCK runtime-8b：`~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/concurrent/ExecutionException/`（含 serial）。

## 4. 测试用例设计 + 易错规范细节
实现：`ExecutionExceptionTest.java`（PASS 32 断言）。
| 场景 | 断言要点 | 优先级 |
|---|---|---|
| 构造器矩阵 | 恰好 2 个 public ctor；无 ()；getSuperclass()==Exception（受检） | P0 |
| (String,null) null-cause 契约 | message 保持 "m"、getCause()==null（**重点**） | P0 |
| ((Throwable)null) | cause 与 message 均为 null | P0 |
| (Throwable) 语义 | cause 恒等；message == cause.toString() | P0 |
| initCause | 已有 cause 后再设 → IllegalStateException | P1 |
| serialVersionUID | == 7830266012832686185（与 CompletionException 同值，实测锁定） | P0 |
| 序列化 round-trip | (String,null) 的消息保持且 cause 仍为 null；带 cause 的类型+消息保持 | P0 |
| 集成来源 | 失败任务的 get() 抛本类型、cause 是任务原始异常对象且消息保持 | P0 |
| 易错细节 | `new ExecutionException()` 编译不过；toString 是 "类名: " + cause.toString() 当用 (Throwable) 构造时 | P1 |
