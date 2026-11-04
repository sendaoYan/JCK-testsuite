# java.util.concurrent.Flow$Subscription 设计案

## 1. API 功能
订阅句柄接口。`request(n)` 请求 n 个元素、`cancel()` 取消订阅。

## 2. Java 用户如何使用
订阅者在 `onSubscribe` 回调中调用 `request(n)` 控制背压；`cancel()` 停止接收。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.SubmissionPublisher` 内部 BufferedSubscription 实现 request/cancel。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：Subscription 接口 request(long)/cancel 方法面。
2. 黑盒：订阅时 request 被调用。
3. 契约深化：手写 DemandSubscriptionPublisher 验证 request 累计语义（request(2)+request(1)+request(MAX)
   分批投递、饱和处理）、request(0)/request(-1) → onError(IllegalArgumentException)、cancel 幂等且其后零投递
   （testCumulativeDemand/testRequestValidation/testCancelSemantics）；SubmissionPublisher 真链路
   （未 request 零投递是因果保证而非时序、request(2) 恰投两条 FIFO、cancel 后 submit 不可见、
   cancelled 订阅者无终态信号）（testSubmissionPublisherSubscription）。

实现：`SubscriptionTest.java`（23 断言）。

## 关键规范细节
- 需求门控是**因果**性质：订阅者未调 request 前不可能收到 onNext（无需 sleep 即可确定断言）。
- cancelled 订阅者连 onComplete 也收不到。
