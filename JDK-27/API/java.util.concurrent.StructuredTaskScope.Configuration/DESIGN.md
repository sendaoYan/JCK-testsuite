# java.util.concurrent.StructuredTaskScope$Configuration 设计案

## 1. API 功能
结构化并发作用域配置接口（预览）。`withThreadFactory`/`withName`/`withTimeout` 流式配置。

## 2. Java 用户如何使用
`StructuredTaskScope.open(UnaryOperator<Configuration>)` 中链式设置名称、超时、线程工厂。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.StructuredTaskScope.open(UnaryOperator)` 传入配置；`test/jdk/java/util/concurrent/structured/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 interface/final/enum/继承 + 反射核对方法签名。
2. 黑盒：SubmissionPublisher/StructuredTaskScope 等真实实现走组合操作链路并断言。

实现：`ConfigurationTest.java`（`testWhiteBox` + `testBlackBox`）。
