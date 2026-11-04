# java.util.concurrent.ThreadPoolExecutor$CallerRunsPolicy 设计案

## 1. API 功能
CallerRunsPolicy 拒绝策略：`rejectedExecution` 在调用线程直接运行任务。

## 2. Java 用户如何使用
线程池饱和时由提交线程自执行，实现背压降级。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy`；`test/jdk/java/util/concurrent/ThreadPoolExecutor/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 class/abstract/final/继承 + 反射核对方法与构造器签名。
2. 黑盒：真实实例化 + 多步组合操作断言（Future/队列/策略/随机范围等）。

实现：`CallerRunsPolicyTest.java`（`testWhiteBox` + `testBlackBox`）。
