# java.util.concurrent.StructuredTaskScope$Joiner 设计案

## 1. API 功能
结构化并发 Joiner 接口（预览）。`result()`/`timeout()` 聚合结果；静态工厂 `allSuccessfulOrThrow`/`anySuccessfulOrThrow`/`awaitAllSuccessfulOrThrow`/`allUntil`。

## 2. Java 用户如何使用
选择聚合策略：`allSuccessfulOrThrow` 全部成功、`anySuccessfulOrThrow` 任一成功、`allUntil` 满足谓词即返回。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.StructuredTaskScope.Joiner`（JEP 480）；`test/jdk/java/util/concurrent/structured/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 interface/final/enum/继承 + 反射核对方法签名。
2. 黑盒：SubmissionPublisher/StructuredTaskScope 等真实实现走组合操作链路并断言。

实现：`JoinerTest.java`（`testWhiteBox` + `testBlackBox`）。
