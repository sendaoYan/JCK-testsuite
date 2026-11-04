# java.util.concurrent.StructuredTaskScope$Subtask 设计案

## 1. API 功能
子任务句柄接口（预览，继承 Supplier）。`state()`/`get()`/`exception()` 查询结果与异常。

## 2. Java 用户如何使用
`scope.fork` 返回 Subtask，`join()` 后 `state()` 判成功/失败、`get()` 取值、`exception()` 取异常。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.StructuredTaskScope.Subtask`（JEP 480）；`test/jdk/java/util/concurrent/structured/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：`Class.forName` 断言 interface/final/enum/继承 + 反射核对方法签名。
2. 黑盒：SubmissionPublisher/StructuredTaskScope 等真实实现走组合操作链路并断言。

实现：`SubtaskTest.java`（`testWhiteBox` + `testBlackBox`）。
