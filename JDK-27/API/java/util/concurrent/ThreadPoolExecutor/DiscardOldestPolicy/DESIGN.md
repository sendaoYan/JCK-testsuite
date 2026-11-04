# java.util.concurrent.ThreadPoolExecutor$DiscardOldestPolicy 设计案

## 1. API 功能
DiscardOldestPolicy 拒绝策略：`rejectedExecution` 丢弃队列中最老任务，再尝试提交新任务。

## 2. Java 用户如何使用
线程池饱和时牺牲最老排队任务以接纳新任务。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy`；`test/jdk/java/util/concurrent/ThreadPoolExecutor/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 class/abstract/final/继承 + 反射核对方法与构造器签名。
2. 黑盒：真实实例化 + 多步组合操作断言（Future/队列/策略/随机范围等）。

实现：`DiscardOldestPolicyTest.java`（`testWhiteBox` + `testBlackBox`）。
