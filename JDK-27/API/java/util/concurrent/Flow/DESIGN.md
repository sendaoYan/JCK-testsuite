# java.util.concurrent.Flow 设计案

## 1. API 功能
Reactive Streams 顶层类。`defaultBufferSize()` 返回默认缓冲大小；含 4 个嵌套接口 Publisher/Subscriber/Subscription/Processor。

## 2. Java 用户如何使用
`Flow.defaultBufferSize()` 作为背压缓冲的默认大小；嵌套类型是响应式流的规范接口。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.Flow` 定义 reactive-streams 4 个角色；`java.util.concurrent.SubmissionPublisher` 实现 Publisher。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：Flow 为 final 类 + defaultBufferSize 方法面；四个嵌套接口存在性与 Processor 直接双继承关系。
2. 黑盒：defaultBufferSize() == 256（javadoc 锁定精确值）+ 跨调用稳定。
3. 契约闭环（手写同步 SyncPublisher + RecordingSub）：onSubscribe 严格先于一切 → onNext×N 按序 →
   onComplete 恰好一次终态；request(1) 需求门控（未请求的不投递）；request(0)/request(-3) →
   onError(IllegalArgumentException)（规则 3.9）；cancel 后不再有任何信号。

实现：`FlowTest.java`（22 断言）。

## 关键规范细节
- defaultBufferSize() javadoc 规定当前为 256，断言精确值而非 >0。
- 手写 Publisher 按 reactive-streams 规则实现，验证的是 Subscriber 四回调契约（顺序/终态唯一性）。
