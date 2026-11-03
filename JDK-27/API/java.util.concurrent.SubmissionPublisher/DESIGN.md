# java.util.concurrent.SubmissionPublisher 设计案

## 1. API 功能
响应式发布者实现（实现 Flow.Publisher + AutoCloseable）。`submit`/`offer` 投递、`subscribe` 注册订阅者、`consume` 收集、`close`/`closeExceptionally` 关闭。

## 2. Java 用户如何使用
用 `SubmissionPublisher` 作为数据源，`subscribe` 订阅者异步接收，`consume` 简便收集所有元素。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.SubmissionPublisher`（JEP 318 后并入 JDK）；`test/jdk/java/util/concurrent/SubmissionPublisher/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 class/abstract/final/继承 + 反射核对方法与构造器签名。
2. 黑盒：真实实例化 + 多步组合操作断言（Future/队列/策略/随机范围等）。

实现：`SubmissionPublisherTest.java`（`testWhiteBox` + `testBlackBox`）。
