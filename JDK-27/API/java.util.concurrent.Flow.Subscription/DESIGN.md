# java.util.concurrent.Flow$Subscription 设计案

## 1. API 功能
订阅句柄接口。`request(n)` 请求 n 个元素、`cancel()` 取消订阅。

## 2. Java 用户如何使用
订阅者在 `onSubscribe` 回调中调用 `request(n)` 控制背压；`cancel()` 停止接收。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.SubmissionPublisher` 内部 BufferedSubscription 实现 request/cancel。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 interface/final/enum/继承 + 反射核对方法签名。
2. 黑盒：SubmissionPublisher/StructuredTaskScope 等真实实现走组合操作链路并断言。

实现：`SubscriptionTest.java`（`testWhiteBox` + `testBlackBox`）。
