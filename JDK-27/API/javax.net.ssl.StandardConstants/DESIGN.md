# javax.net.ssl.StandardConstants 黑盒行为测试设计

## 1. API 功能
final 类，SNI 常量：
- `SNI_HOST_NAME = 0`。

## 2. Java 用户如何使用
`StandardConstants.SNI_HOST_NAME` 用于 SNIServerName 的 type 参数。

## 3. 开源代码实际用法调研
JDK `SNIHostName` 构造即 `super(StandardConstants.SNI_HOST_NAME, ...)`。

## 4. 测试用例设计 + 自动化实现
黑盒：SNI_HOST_NAME == 0。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。
