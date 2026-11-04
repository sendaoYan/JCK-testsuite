# java.util.concurrent.BrokenBarrierException 设计案

## 1. API 功能
受检异常（extends Exception）：`CyclicBarrier` 处于 broken 状态（某参与者中断/超时/失败，或屏障被 reset）时，`await()` 抛出。构造器仅两个：`()` 与 `(String message)`；无 cause 构造器（cause 只能经 `initCause` 补设）。可序列化（经 Throwable）。

## 2. Java 用户如何使用（含开源实际用法调研）
- 典型来源：多线程分阶段计算中某线程超时后，其余等待者在 `await()` 处收到本异常统一退出；`CyclicBarrier.reset()` 也会使在途等待者收到。
- 实现方常 catch 后转译为业务语义（如 Spark/RxJava 把阶段失败聚合上报），依赖 `getMessage()` 的精确保持。
- 开源用法：Elasticsearch/Guava 的测试工具在屏障协议里捕获该异常断言协作失败路径。

## 3. 参考来源
- Oracle javadoc：java.util.concurrent.BrokenBarrierException（构造器矩阵、无 cause 构造器说明）。
- OpenJDK：`~/git/jdk8u-dev/jdk/test/java/util/concurrent/CyclicBarrier/`（broken 屏障触发路径）。
- JCK runtime-8b：`~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/concurrent/BrokenBarrierException/`（API + serial 子目录，确认序列化契约属权威覆盖范围）。

## 4. 测试用例设计 + 易错规范细节
实现：`BrokenBarrierExceptionTest.java`（PASS 34 断言）。
| 场景 | 断言要点 | 优先级 |
|---|---|---|
| 构造器矩阵 | 恰好 2 个 public ctor（()/(String)），无 (String,Throwable)；getSuperclass()==Exception 精确（非 RuntimeException） | P0 |
| message/toString | getMessage 精确保持；toString == "全限定名: 消息"；无消息时仅类名；getCause 初始 null | P0 |
| initCause | 返回 this、cause 生效；二次 initCause → IllegalStateException | P1 |
| fillInStackTrace/StackTrace | 返回 this；构造时栈非空；setStackTrace 空数组/固定元素生效 | P1 |
| serialVersionUID | ObjectStreamClass.lookup == 7117394618823254244（实测锁定） | P0 |
| 序列化 round-trip | 消息/null cause 保持；initCause 设的 cause 类型+消息保持；toString 前后相等 | P0 |
| 易错细节 | (String,null) 型构造器不存在于本类（别照搬 REE 的 4 ctor）；cause 不会从 (String) ctor 传入 | P1 |
