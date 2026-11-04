# java.util.concurrent.ForkJoinPool$ManagedBlocker 设计案

## 1. API 功能
ForkJoinPool 托管阻塞接口。`block()` 执行阻塞、`isReleasable()` 判断是否可跳过阻塞；由 `ForkJoinPool.managedBlock` 调度。

## 2. Java 用户如何使用
在 fork/join 任务内做可能阻塞的操作时，通过 `ForkJoinPool.managedBlock` 让池补偿工作线程。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.ForkJoinPool.managedBlock`；`java.util.concurrent.Phaser`/`CountedCompleter` 内部使用。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 interface/enum/继承层次 + 反射核对关键方法签名。
2. 黑盒：用具体实现（LinkedBlockingQueue/ConcurrentHashMap/Executor 族等）走多步组合操作并断言可观察行为。

实现：`ManagedBlockerTest.java`（`testWhiteBox` + `testBlackBox`）。
