# java.util.concurrent.StructuredTaskScope$Subtask$State 设计案

## 1. API 功能
子任务状态枚举（预览）：`UNAVAILABLE`/`SUCCESS`/`FAILED`。

## 2. Java 用户如何使用
`subtask.state()` 判断子任务尚未完成/成功/失败。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.StructuredTaskScope.Subtask.State`（JEP 480）；`test/jdk/java/util/concurrent/structured/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 interface/final/enum/继承 + 反射核对方法签名。
2. 黑盒：SubmissionPublisher/StructuredTaskScope 等真实实现走组合操作链路并断言。

实现：`StateTest.java`（`testWhiteBox` + `testBlackBox`）。
