# javax.net.ssl.SSLEngineResult$Status 黑盒行为测试设计

## 1. API 功能
枚举：BUFFER_UNDERFLOW/BUFFER_OVERFLOW/OK/CLOSED（4 常量）。

## 2. Java 用户如何使用
wrap/unwrap 结果状态。

## 3. 开源代码实际用法调研
JDK TLSCommon 模板判定。

## 4. 测试用例设计 + 自动化实现
黑盒：values 长度 4、valueOf。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。
