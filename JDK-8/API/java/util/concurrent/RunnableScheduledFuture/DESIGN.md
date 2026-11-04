# java.util.concurrent.RunnableScheduledFuture 设计案

## 1. API 功能
接口 `<V> extends RunnableFuture<V>, ScheduledFuture<V>`：可执行的定时任务视图，唯一自有方法 `boolean isPeriodic()`。ScheduledThreadPoolExecutor 的三种 schedule 形态（一次性/fixedRate/fixedDelay）产物均实现本接口（ScheduledFutureTask）。

## 2. Java 用户如何使用（含开源实际用法调研）
- 典型用法：从 STPE 拿到返回值后判周期性（重试/心跳类任务取消策略不同）；自定义 STPE 子类覆写 `decorateTask` 换产物实现（Netty/Hazelcast 的调度器同构）。
- 周期性语义：isPeriodic() true 表示 fixedRate/fixedDelay（re-execution），false 表示一次性。
- 与队列：取消后的任务在默认 removeOnCancelPolicy=false 下仍留队，purge() 清除。

## 3. 参考来源
- Oracle javadoc：RunnableScheduledFuture、ScheduledThreadPoolExecutor（decorateTask、removeOnCancelPolicy）。
- OpenJDK：`~/git/jdk8u-dev/jdk/test/java/util/concurrent/ScheduledThreadPoolExecutor/`。
- JCK runtime-8b：`~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_util/concurrent/ScheduledExecutor/`。

## 4. 测试用例设计 + 易错规范细节
实现：`RunnableScheduledFutureTest.java`（PASS 26 断言）。
| 场景 | 断言要点 | 优先级 |
|---|---|---|
| 白盒结构 | isInterface；extends RunnableFuture+ScheduledFuture（Runnable/Future/Delayed 全链 assignable）；唯一声明方法 isPeriodic() | P0 |
| 产物类型矩阵 | schedule/scheduleAtFixedRate/scheduleWithFixedDelay 三种产物均 instanceof 本接口 | P0 |
| 周期性矩阵 | 一次性 false；fixedRate true；fixedDelay true | P0 |
| 延迟面 | 2 小时任务 getDelay(HOURS)∈[0,2]、SECONDS∈(0,7200]；compareTo 反身 0 | P1 |
| cancel×队列 | 默认策略下 cancel 后任务仍留队（size==1 且元素即该 future）、purge() 后清空（**实测记录**，8/27 一致） | P1 |
| 易错细节 | getDelay 会随时间衰减，只能区间断言；purge() 返回 void（无返回值断言） | P1 |
