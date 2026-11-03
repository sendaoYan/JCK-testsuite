# java.util.concurrent.Flow$Subscriber 设计案

## 1. API 功能
订阅者接口。`onSubscribe`/`onNext`/`onError`/`onComplete` 四个回调；`onSubscribe` 中调用 `request(n)` 拉取。

## 2. Java 用户如何使用
订阅 SubmissionPublisher 后，`onSubscribe` 发起 request，随后 `onNext` 逐个接收元素。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.SubmissionPublisher` 调用订阅者四回调；`test/jdk/java/util/concurrent/SubmissionPublisher/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 interface/final/enum/继承 + 反射核对方法签名。
2. 黑盒：SubmissionPublisher/StructuredTaskScope 等真实实现走组合操作链路并断言。

实现：`SubscriberTest.java`（`testWhiteBox` + `testBlackBox`）。
