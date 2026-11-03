# javax.net.ssl.SSLException 黑盒行为测试设计

## 1. API 功能
异常类，extends java.io.IOException：`SSLException(String)`、`(String,Throwable)`、`(Throwable)`。

## 2. Java 用户如何使用
catch SSLException 处理 TLS 层错误；子类有 SSLHandshakeException/SSLKeyException/SSLPeerUnverifiedException/SSLProtocolException。

## 3. 开源代码实际用法调研
JDK `test/jdk/javax/net/ssl/SSLException/` 覆盖；TLS 库广泛捕获。

## 4. 测试用例设计 + 自动化实现
黑盒：三个构造的 message/cause；is IOException。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。
