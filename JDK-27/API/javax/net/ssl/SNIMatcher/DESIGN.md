# javax.net.ssl.SNIMatcher 黑盒行为测试设计

## 1. API 功能
抽象类，服务端 SNI 匹配器：
- `SNIMatcher(int type)`：protected，负 type 抛 IAE。
- `getType()`；`abstract matches(SNIServerName)`。
- 标准子类由 `SNIHostName.createSNIMatcher(regex)` 创建。

## 2. Java 用户如何使用
`new SNIHostName.createSNIMatcher(".*\.example\.com")` 配置到 `SSLParameters.setSNIMatchers`。

## 3. 开源代码实际用法调研
JDK `test/jdk/javax/net/ssl/ServerName/` 覆盖 SNIMatcher；RFC 6066 服务端 SNI 选择。

## 4. 测试用例设计 + 自动化实现
黑盒：createSNIMatcher 匹配/不匹配；自定义 matcher type + matches；负 type 抛 IAE。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。
