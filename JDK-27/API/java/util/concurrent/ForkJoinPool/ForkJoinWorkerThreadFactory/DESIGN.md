# java.util.concurrent.ForkJoinPool$ForkJoinWorkerThreadFactory 设计案

## 1. API 功能
ForkJoinPool 工作线程工厂接口。`newThread(ForkJoinPool)` 为池创建 `ForkJoinWorkerThread`。

## 2. Java 用户如何使用
自定义 ForkJoinPool 时提供线程工厂，控制工作线程的线程组、优先级、守护属性。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.ForkJoinPool` 默认 `defaultForkJoinWorkerThreadFactory` 实现该接口。

## 4. 测试用例设计 + 自动化实现
场景（26 断言）：
1. 白盒：接口 + newThread(ForkJoinPool) 存在；恰一个抽象方法（函数式接口）；返回类型 ForkJoinWorkerThread；非 default；`ForkJoinPool.class.getField("defaultForkJoinWorkerThreadFactory")` 反射读取非空 + 接口实例 + 直接常量同一。
2. 黑盒：默认池工厂创建线程（绑定池/UEH 非空）。
3. 默认工厂：daemon、ForkJoinPool- 前缀、getPoolIndex>=0、newThread(null) → NPE。
4. RecordingWorker 自定义工厂：RecWorker- 前缀、getPool==pool、daemon、UEH 安装、子类实例。
5. null 返回工厂：任务不执行、池仍可终止（确定性）。

易错规范细节：工厂字段经 ForkJoinPool.class.getField 读取（声明在池上而非接口）；null 工厂场景用布尔标志断言而非等待。

实现：`ForkJoinWorkerThreadFactoryTest.java`（26 断言）。优先级：默认工厂契约 > 自定义工厂集成 > null 边界。
