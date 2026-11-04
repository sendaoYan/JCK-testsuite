# java.util.concurrent.RunnableFuture 设计案

## 1. API 功能
接口 `<V> extends Runnable, Future<V>`：可被**执行**的 Future（run() 触发计算并完成自身）。唯一直接声明方法 `run()`。实现：FutureTask（通用）、ScheduledThreadPoolExecutor.ScheduledFutureTask（经 RunnableScheduledFuture）。

## 2. Java 用户如何使用（含开源实际用法调研）
- 典型用法：`new FutureTask<>(callable)` 既是 Runnable（可交给线程/池）又是 Future（可取结果）；Swing `SwingWorker` 内部同构。
- 开源用法：ForkJoinTask 的适配、Guava `AbstractFuture`+trampoline、`ExecutorCompletionService` 队列元素即 RunnableFuture。
- 核心行为契约：run 幂等（只算一次）；取消后 run 无效；run 完成 Future。

## 3. 参考来源
- Oracle javadoc：RunnableFuture（"A Future that is Runnable"）。
- OpenJDK：`~/git/jdk8u-dev/jdk/test/java/util/concurrent/FutureTask/`（run/cancel 幂等路径）。
- JCK runtime-8b：`~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/concurrent/FutureTask/`。

## 4. 测试用例设计 + 易错规范细节
实现：`RunnableFutureTest.java`（PASS 22 断言）。
| 场景 | 断言要点 | 优先级 |
|---|---|---|
| 白盒结构 | isInterface；直接父接口恰为 Runnable+Future；唯一声明方法 run()；FutureTask/RunnableScheduledFuture assignable | P0 |
| run 幂等 | 两次 run() 后 callable 计数仍为 1、结果/状态稳定 | P0 |
| cancel 后 run | cancel(true) 后 run 不执行 callable、isCancelled&&isDone | P0 |
| Runnable+V | FutureTask(Runnable,"fixed") 经接口类型 run 后 get=="fixed" | P1 |
| 线程执行 | 作为 Runnable 交给 Thread 跑，join(5s) 后 isDone 且 get==值 | P1 |
| 易错细节 | run 后再 cancel 恒 false；深行为（setException/Runnable#run 异常）归 FutureTask 批次 | P1 |
