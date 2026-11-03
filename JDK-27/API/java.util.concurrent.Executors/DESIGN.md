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

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：final 类、无公共构造器、关键静态工厂/适配方法存在。
2. 黑盒：各工厂执行任务并 terminate（含虚拟线程工厂）、`defaultThreadFactory` 命名且不 start、`callable(Runnable)` 返回 null 且运行、`callable(Runnable,T)` 返回 T、`unconfigurableExecutorService` 委托执行。

实现：`ExecutorsTest.java`（31 断言）。
