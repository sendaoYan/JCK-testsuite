# javax.net.ssl.SSLContextSpi 黑盒行为测试设计

## 1. API 功能
抽象 SPI：`engineInit`、`engineGetSocketFactory`、`engineGetServerSocketFactory`、`engineCreateSSLEngine()`/`(host,port)`、`engineGetServerSessionContext`/`engineGetClientSessionContext`，另有 protected 默认 `engineGetDefaultSSLParameters`/`engineGetSupportedSSLParameters`。

## 2. Java 用户如何使用
provider 实现者继承并实现 engine* 方法，供 SSLContext 调用。

## 3. 开源代码实际用法调研
`~/git/jdk25u-dev/src/java.base/share/classes/sun/security/ssl/SSLContextImpl.java` 即该 SPI 的 SunJSSE 实现。

## 4. 测试用例设计 + 自动化实现
黑盒：可子类化；结构断言（抽象类）。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。
