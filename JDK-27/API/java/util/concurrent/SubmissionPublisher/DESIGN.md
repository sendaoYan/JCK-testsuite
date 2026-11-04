# java.util.concurrent.SubmissionPublisher 设计案

## 1. API 功能
响应式发布者实现（实现 Flow.Publisher + AutoCloseable）。`submit`/`offer` 投递、`subscribe` 注册订阅者、`consume` 收集、`close`/`closeExceptionally` 关闭。

## 2. Java 用户如何使用
用 `SubmissionPublisher` 作为数据源，`subscribe` 订阅者异步接收，`consume` 简便收集所有元素。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.SubmissionPublisher`（JEP 318 后并入 JDK）；`test/jdk/java/util/concurrent/SubmissionPublisher/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：类 + 实现 Flow.Publisher/AutoCloseable + 12 个方法面。
2. 黑盒主干：初始状态/注册投递/consume 收集/closeExceptionally/估计量非负。
3. 契约深化：默认容量 == Flow.defaultBufferSize()==256、自定义 executor 恒等返回、容量 7→8 幂规整
   （testConstructorsAndDefaults）；submit lag 估计 + 回调在 executor 线程 + close→onComplete + close 幂等 +
   submit after close ISE("Closed")（testSubmitLagAndDelivery）；closeExceptionally 对象恒等 + 先正常 close 后
   closeExceptionally 无效（testCloseExceptionallyContract）；订阅簿记（重订阅去重不抛、subscribe(null) NPE、
   getSubscribers 独立快照）（testSubscriptionBookkeeping）；offer 满缓冲无需求 → -1（带超时不阻塞）+
   有需求成功（testOfferWithFullBuffer）；订阅者 onNext 抛异常 → onError 收自己的异常 + 被退订
   （testSubscriberFailureUnsubscribes）；consume future 在 close 完成且保序（testConsumeCompletes）。

实现：`SubmissionPublisherTest.java`（74 断言）。

## 关键规范细节（实测锁定）
- maxBufferCapacity 向上规整到 2 的幂（7→8、9→16）。
- close 幂等（二次 close 不抛）；submit after close → IllegalStateException("Closed")。
- 重复 subscribe 同一订阅者：**不抛**、计数不增（去重）；subscribe(null) → NPE。
- 订阅者回调发生在构造时给定的 executor 线程（线程名断言）。
- 订阅者 onNext 抛异常 → 其 onError 收到**同一个异常对象**且被移出订阅列表。
