# java.util.concurrent.ForkJoinWorkerThread 设计案

## 1. API 功能
ForkJoinPool 工作线程（继承 Thread）。`getPool`/`getPoolIndex`/`getQueuedTaskCount`；可覆写 `onStart`/`onTermination`。

## 2. Java 用户如何使用
自定义 ForkJoinPool 时用 `ForkJoinPool.ForkJoinWorkerThreadFactory` 创建该线程并查询其归属池。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory`；`test/jdk/java/util/concurrent/ForkJoinPool/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 class/abstract/final/继承 + 反射核对方法与构造器签名。
2. 黑盒：真实实例化 + 多步组合操作断言（Future/队列/策略/随机范围等）。

实现：`ForkJoinWorkerThreadTest.java`（`testWhiteBox` + `testBlackBox`）。
