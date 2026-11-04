# java.util.concurrent.Flow$Subscriber 设计案

## 1. API 功能
订阅者接口。`onSubscribe`/`onNext`/`onError`/`onComplete` 四个回调；`onSubscribe` 中调用 `request(n)` 拉取。

## 2. Java 用户如何使用
订阅 SubmissionPublisher 后，`onSubscribe` 发起 request，随后 `onNext` 逐个接收元素。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.SubmissionPublisher` 调用订阅者四回调；`test/jdk/java/util/concurrent/SubmissionPublisher/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：Subscriber 接口四方法面。
2. 黑盒：submit→onNext 投递。
3. 契约深化：手写 ContractPublisher 驱动回调顺序（onSubscribe 先于 onNext、onComplete 唯一终态、
   终态后零信号）（testCallbackOrdering）；非正 request → onError(IllegalArgumentException) 且其后零信号
   （testErrorPath）；SubmissionPublisher 全链（onSubscribe/onNext×2 按序/onComplete after close、
   closeExceptionally → onError 对象恒等）（testSubmissionPublisherDelivery）。

实现：`SubscriberTest.java`（21 断言）。

## 关键规范细节
- onError/onComplete 互斥且至多一次（终态唯一性）。
- closeExceptionally 的异常对象经 onError **恒等**传递。
