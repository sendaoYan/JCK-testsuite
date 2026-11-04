# java.util.concurrent.Flow$Publisher 设计案

## 1. API 功能
发布者接口。`subscribe(Subscriber)` 注册订阅者；实现背压协议。

## 2. Java 用户如何使用
`SubmissionPublisher` 实现 Publisher，`subscribe` 后异步投递元素。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.SubmissionPublisher.subscribe`；`test/jdk/java/util/concurrent/SubmissionPublisher/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 interface/final/enum/继承 + 反射核对方法签名。
2. 黑盒：SubmissionPublisher/StructuredTaskScope 等真实实现走组合操作链路并断言。

实现：`PublisherTest.java`（`testWhiteBox` + `testBlackBox`）。
