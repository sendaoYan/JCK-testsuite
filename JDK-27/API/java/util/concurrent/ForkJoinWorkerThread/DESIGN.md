# java.util.concurrent.ForkJoinWorkerThread 设计案

## 1. API 功能
ForkJoinPool 工作线程（继承 Thread）。`getPool`/`getPoolIndex`/`getQueuedTaskCount`；可覆写 `onStart`/`onTermination`。

## 2. Java 用户如何使用
自定义 ForkJoinPool 时用 `ForkJoinPool.ForkJoinWorkerThreadFactory` 创建该线程并查询其归属池。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory`；`test/jdk/java/util/concurrent/ForkJoinPool/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景（26 断言）：
1. 白盒：类/extends Thread/getPool/getPoolIndex/getQueuedTaskCount/run + onStart/onTermination 声明存在。
2. 黑盒：默认工厂创建 worker（绑定池、getPoolIndex、getQueuedTaskCount==0、daemon、ForkJoinPool- 前缀）。
3. 自定义子类实例化：构造时绑定池、poolIndex>=0、daemon。
4. 运行中身份：pool.invoke 内 getPool==pool、daemon、默认名、poolIndex>=0、**getQueuedTaskCount()>=0（JDK-9+ 实例 API，运行中可调用）**。
5. 自定义命名 worker（Cw- 前缀）。
6. 生命周期钩子：onStart>=1、shutdownNow+awaitTermination 后 onTermination>=1。

易错规范细节：身份断言只走自定义池 pool.invoke（确定性在 worker 执行）；钩子计数 AtomicInteger；池 finally shutdownNow。

实现：`ForkJoinWorkerThreadTest.java`（26 断言）。优先级：身份/钩子契约 > JDK-9+ getQueuedTaskCount > 命名。
