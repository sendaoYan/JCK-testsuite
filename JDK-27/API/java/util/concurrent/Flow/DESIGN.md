# java.util.concurrent.Flow 设计案

## 1. API 功能
Reactive Streams 顶层类。`defaultBufferSize()` 返回默认缓冲大小；含 4 个嵌套接口 Publisher/Subscriber/Subscription/Processor。

## 2. Java 用户如何使用
`Flow.defaultBufferSize()` 作为背压缓冲的默认大小；嵌套类型是响应式流的规范接口。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.Flow` 定义 reactive-streams 4 个角色；`java.util.concurrent.SubmissionPublisher` 实现 Publisher。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 interface/final/enum/继承 + 反射核对方法签名。
2. 黑盒：SubmissionPublisher/StructuredTaskScope 等真实实现走组合操作链路并断言。

实现：`FlowTest.java`（`testWhiteBox` + `testBlackBox`）。
