# java.util.concurrent.Executors 设计案

## 1. API 功能
final 工具类（私有构造器），静态工厂与适配器。工厂：`newSingleThreadExecutor`/`newFixedThreadPool`/`newCachedThreadPool`/`newScheduledThreadPool`/`newSingleThreadScheduledExecutor`/`newWorkStealingPool`/`newVirtualThreadPerTaskExecutor`。适配：`callable(Runnable[,T])`、`unconfigurableExecutorService`、`defaultThreadFactory`/`privilegedThreadFactory`。

## 2. Java 用户如何使用
- 工厂：`ExecutorService es = Executors.newFixedThreadPool(2); es.submit(...).get(); es.shutdown();`。
- 线程工厂：`ThreadFactory tf = Executors.defaultThreadFactory(); Thread t = tf.newThread(r);`（命名、不自动 start）。
- 适配：`Callable<Object> c = Executors.callable((Runnable) ...)`。

## 3. 开源代码实际用法调研
- JDK 自带测试：`test/jdk/java/util/concurrent/Executors/`、`tck/ExecutorsTest.java` 覆盖各工厂与 callable 适配。
- 框架：Spring `ThreadPoolTaskExecutor` 底层用 `Executors.newFixedThreadPool`；Guava `MoreExecutors` 大量包装该工具。

## 4. 测试用例设计 + 自动化实现 + 易错规范细节 + 优先级
实现：`ExecutorsTest.java`（121 断言；深化前 31）。

### 深化前场景（保留全部断言）
白盒：final 类、无公共构造器、静态工厂/适配方法存在。黑盒：工厂执行并 terminate、defaultThreadFactory 命名且不 start、callable(Runnable[,T])、unconfigurable 委托。

### 深化新增场景

| 场景 | 断言要点 | 优先级 |
|---|---|---|
| newFixedThreadPool(2) 并行度 | CyclicBarrier(2) 证明恰好 2 并行；三方屏障 3s 超时永不成立证明上界 2（无墙钟断言） | P0 |
| newSingleThreadExecutor 串行 | 提交序 1,2,3 全等 + 三任务共用同一线程 | P0 |
| newCachedThreadPool | 4 并发阻塞任务恰好 4 线程；命名 `pool-\d+-thread-\d+`；第二轮仅断言总线程 ∈[4,8]（复用存在 offer/park 竞态） | P0 |
| newWorkStealingPool(2) | 屏障证明 2 并行 | P1 |
| defaultThreadFactory | 命名递增、daemon=false、NORM_PRIORITY、未启动、newThread(null) 合法返回未启动线程（null runnable 非 NPE——实测） | P0 |
| 自定义 ThreadFactory 透传 | 名/daemon/优先级在任务内经 Thread.currentThread() 观察（fixed/single/cached 重载） | P0 |
| callable 适配族 | (Runnable)→null、(Runnable,T)→T、PrivilegedAction/PEA→结果、PEA 抛出→PrivilegedActionException cause 原样、null→NPE 消息 null | P0 |
| privileged 系 | 无 SecurityManager 下行为不变（deprecated for removal 仍可用，@SuppressWarnings）；privilegedThreadFactory 命名 | P2 |
| newThreadPerTask/newVirtualThreadPerTask | 每 submit 一线程（threadLocal 观察不同线程）；虚拟工厂 isVirtual()==true（JDK-21+ 新增，JDK-8 无） | P1 |
| Scheduled 工厂 | 0 延迟执行、schedule(Callable) 值精确、TF 重载名字透传 | P1 |
| unconfigurable 包装 | 委托 submit、shutdown 传播到委托池、Scheduled 包装委托 schedule | P1 |
| 工厂负例 | newFixedThreadPool(0/-1)→IAE **消息 "maximumPoolSize must be positive"**；newScheduledThreadPool(-1)→IAE "corePoolSize must be non-negative"、(0) 合法；unconfigurable(null)→NPE 消息 null | P0 |

易错规范细节：
1. 并行度双向证明（能凑齐=下界、3s 超时凑不齐=上界），不做时距断言。
2. cached 池线程复用非确定（worker poll 与新任务 offer 竞态），第二轮只能断言上界区间。
3. JDK-27 IAE/NPE 均带描述性消息，须实测后精确断言（null 与非 null 分支都要覆盖）。

## 版本差异（vs JDK-8）
JDK-8 非 final、无 newThreadPerTaskExecutor/newVirtualThreadPerTaskExecutor、privileged 系未废弃；JDK-8 工厂负例消息全 null，JDK-27 为 "maximumPoolSize must be positive"/"corePoolSize must be non-negative"。
