# java.util.concurrent.Flow$Publisher 设计案

## 1. API 功能
发布者接口。`subscribe(Subscriber)` 注册订阅者；实现背压协议。

## 2. Java 用户如何使用
`SubmissionPublisher` 实现 Publisher，`subscribe` 后异步投递元素。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.SubmissionPublisher.subscribe`；`test/jdk/java/util/concurrent/SubmissionPublisher/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：Publisher 接口 + subscribe(Flow.Subscriber) 方法面。
2. 黑盒：SubmissionPublisher 注册订阅者。
3. 契约深化：手写 RangePublisher 同步发布（request 门控、cancel 停发、FIFO 序、两个独立订阅者各自收全）
   （testHandWrittenPublisherContract）；接口类型驱动的 SubmissionPublisher 发布（submit→onNext 精确值，
   自定义 daemon executor）（testSubmissionPublisherAsPublisher）。

实现：`PublisherTest.java`（13 断言）。

## 关键规范细节
- 手写 Publisher 的投递严格受 demand 门控（request(1) 只投一条）。
- cancel 后 request 不再投递（spec 3.5/3.6）。
