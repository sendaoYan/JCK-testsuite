# java.util.concurrent.ForkJoinPool$ForkJoinWorkerThreadFactory 设计案

## 1. API 功能
ForkJoinPool 工作线程工厂接口。`newThread(ForkJoinPool)` 为池创建 `ForkJoinWorkerThread`。

## 2. Java 用户如何使用
自定义 ForkJoinPool 时提供线程工厂，控制工作线程的线程组、优先级、守护属性。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.ForkJoinPool` 默认 `defaultForkJoinWorkerThreadFactory` 实现该接口。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 interface/enum/继承层次 + 反射核对关键方法签名。
2. 黑盒：用具体实现（LinkedBlockingQueue/ConcurrentHashMap/Executor 族等）走多步组合操作并断言可观察行为。

实现：`ForkJoinWorkerThreadFactoryTest.java`（`testWhiteBox` + `testBlackBox`）。
