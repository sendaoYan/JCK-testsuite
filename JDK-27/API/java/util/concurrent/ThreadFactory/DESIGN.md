# java.util.concurrent.ThreadFactory 设计案

## 1. API 功能
函数式接口：`Thread newThread(Runnable r)`。按需创建线程（可自定义命名/daemon/优先级/线程组），供线程池统一制造 worker 线程。

## 2. Java 用户如何使用
- 实现：`ThreadFactory tf = r -> new Thread(r, "worker-" + i.getAndIncrement())`。
- 链路：`tf.newThread(task)` 返回**未启动**线程；交给 `Executors.newFixedThreadPool(n, tf)` 后由池启动；`Executors.defaultThreadFactory()` 提供默认命名。

## 3. 开源代码实际用法调研
- JDK 自用：`Executors.defaultThreadFactory`/`privilegedThreadFactory` 为其实现；`ThreadPoolExecutor` 以 threadFactory 生产 worker。
- JDK 自带测试：`tck/ExecutorsTest.java`、`test/jdk/java/util/concurrent/Executors/`。
- 框架：Spring `CustomizableThreadFactory`、Guava `ThreadFactoryBuilder` 实现该接口做统一命名。

## 4. 测试用例设计 + 自动化实现
场景（深化批次追加 defaultThreadFactory 契约与组合模式）：
1. 白盒：接口、单抽象方法 `newThread(Runnable)`；**无 @FunctionalInterface 注解（实测）**。
2. 黑盒：命名序列、不自动 start、daemon 标志生效、start 返回线程会运行传入 Runnable。
3. `Executors.defaultThreadFactory()` 产物契约：非 daemon、NORM_PRIORITY、state==NEW、名字 matches `pool-\d+-thread-\d+`、同工厂线程号递增（解析名字数字差 1）、`newThread(null)` 返回 Thread 不抛（**实测记录**，与 JDK-8 一致）。
4. 工厂装饰器：计数 + 委托 defaultThreadFactory 的组合工厂产出数正确。

实现：`ThreadFactoryTest.java`（`testWhiteBox` + `testBlackBox` + `testDefaultThreadFactory` + `testAnnotationAndComposition`，PASS 20 断言）。
