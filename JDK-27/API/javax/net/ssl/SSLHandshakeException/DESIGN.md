# javax.net.ssl.SSLHandshakeException 黑盒行为测试设计

## 1. API 功能
异常类，extends javax.net.ssl.SSLException：`(String)`、`(String,Throwable)`。

## 2. Java 用户如何使用
catch SSLHandshakeException 处理对应 TLS 失败场景。

## 3. 开源代码实际用法调研
JDK `test/jdk/javax/net/ssl/` 相关测试触发该异常；TLS 库捕获。

## 4. 测试用例设计 + 自动化实现
黑盒：message / cause；is SSLException / is IOException。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。
