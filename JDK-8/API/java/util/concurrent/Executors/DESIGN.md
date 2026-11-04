# java.util.concurrent.Executors 设计案（JDK-8）

## 1. API 功能
工具类（JDK-8 非 final，JDK-17+ 改为 final——版本差异），私有构造器，全部为静态工厂与适配器：
- 固定/单线程/缓存池：`newFixedThreadPool(int[,ThreadFactory])`、`newSingleThreadExecutor([ThreadFactory])`、`newCachedThreadPool([ThreadFactory])`。
- 工作窃取：`newWorkStealingPool([int])`（JDK-8 引入）。
- 调度池：`newScheduledThreadPool(int[,ThreadFactory])`、`newSingleThreadScheduledExecutor([ThreadFactory])`（归 Scheduled 批次）。
- 包装：`unconfigurableExecutorService(ExecutorService)`、`unconfigurableScheduledExecutorService(ScheduledExecutorService)`。
- 线程工厂：`defaultThreadFactory()`（pool-N-thread-M 命名、非 daemon、NORM_PRIORITY）、`privilegedThreadFactory()`（JDK-8 未废弃；JDK-17+ 标记移除）。
- Callable 适配：`callable(Runnable)`、`callable(Runnable,T)`、`callable(PrivilegedAction)`、`callable(PrivilegedExceptionAction)`、`privilegedCallable(Callable)`、`privilegedCallableUsingCurrentClassLoader(Callable)`（后三个 JDK-17+ 废弃）。

## 2. Java 用户如何使用 + 开源用法调研
- 工厂：`ExecutorService es = Executors.newFixedThreadPool(2); es.submit(...).get(); finally es.shutdown();`
- 线程工厂：自定义命名/daemon/优先级经工厂透传到池内线程（任务内 `Thread.currentThread()` 观察）。
- 适配：把 `Runnable`+结果值包成 `Callable`；`PrivilegedAction` 适配在无 SecurityManager 环境退化为直接调用。
- 开源调研：OpenJDK `test/jdk/java/util/concurrent/Executors/`、JSR-166 TCK `ExecutorsTest` 覆盖各工厂与适配器；Spring `ThreadPoolTaskExecutor` 底层用 `newFixedThreadPool`；Guava `MoreExecutors` 大量包装；Netty `DefaultExecutorThreadFactory` 参考 defaultThreadFactory 命名。

## 3. 参考来源
- Oracle javadoc（java.util.concurrent.Executors，Java 8 版）：工厂语义、线程命名规则、异常契约。
- OpenJDK 8u 源码 `Executors.java`（消息/边界行为核对）。
- 双侧 javap 实测：`jdk8u372-b07` vs `jdk-27.0.1.0.2+1`（newThreadPerTaskExecutor/newVirtualThreadPerTaskExecutor 为 JDK-21+ 新增）。

## 4. 测试用例设计 + 易错规范细节 + 优先级表
实现：`ExecutorsTest.java`（75 断言；深化前 2）。

| 场景 | 断言要点 | 优先级 |
|---|---|---|
| newFixedThreadPool(2) 并行度 | CyclicBarrier(2) 证明恰好 2 并行；三方屏障永不成立证明上界 2 | P0 |
| newSingleThreadExecutor 串行 | 提交序 1,2,3 全等 + 三任务共用 1 线程 | P0 |
| newCachedThreadPool | 4 并发阻塞任务恰好 4 线程；名匹配 `pool-\d+-thread-\d+`；第二轮仅断言总线程数 ∈[4,8] | P0 |
| newWorkStealingPool(2) | 屏障证明 2 并行 | P1 |
| defaultThreadFactory | 命名模式/递增、daemon=false、NORM_PRIORITY、未启动、**newThread(null) 合法返回未启动线程（null runnable 合法，非 NPE——实测）** | P0 |
| 自定义 ThreadFactory 透传 | 名字/daemon/优先级在任务内观察（fixed/single/cached 三工厂重载） | P0 |
| callable 适配器族 | (Runnable)→null 且执行、(Runnable,T)→T、PrivilegedAction/PEA→结果、PEA 抛出→PrivilegedActionException 原样透传（cause==原实例）、null 参数 NPE | P0 |
| privileged 系 | 无 SecurityManager 下 privilegedCallable/privilegedCallableUsingCurrentClassLoader/privilegedThreadFactory 均可用且值/命名正确 | P2 |
| Scheduled 工厂 | 0 延迟执行、schedule(Callable) 值精确、TF 重载名字透传 | P1 |
| unconfigurable 包装 | 委托 submit、shutdown 传播到委托池、Scheduled 包装委托 schedule | P1 |
| 工厂负例 | newFixedThreadPool(0/-1)→IAE（**JDK-8 消息 null**）、newScheduledThreadPool(-1)→IAE、(0) 合法、unconfigurable(null)→NPE | P0 |

易错规范细节：
1. 并行度断言禁止墙钟：用 barrier 双向证明（能凑齐=下界、凑不齐=上界）。
2. cached 复用线程存在 offer/park 竞态，第二轮不可断言"无线程新建"，只能断言上界。
3. JDK-8 的 IAE/NPE 消息为 null，与 JDK-9+ 描述性消息不同，断言须分侧。
4. Executors 在 JDK-8 非 final（JDK-17+ final），白盒断言分侧。

## 版本差异（JDK-8 vs JDK-27）
- JDK-27 新增 `newThreadPerTaskExecutor(ThreadFactory)`、`newVirtualThreadPerTaskExecutor()`；类为 final。
- JDK-27 的 `privilegedCallable`/`privilegedCallableUsingCurrentClassLoader`/`privilegedThreadFactory` 标记 deprecated for removal（行为不变）。
- 工厂负例消息：JDK-8 全部 null；JDK-27 为 "maximumPoolSize must be positive"/"corePoolSize must be non-negative" 等。
