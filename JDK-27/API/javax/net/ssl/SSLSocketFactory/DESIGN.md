# javax.net.ssl.SSLSocketFactory 黑盒行为测试设计

## 1. API 功能
抽象类 extends javax.net.SocketFactory：`getDefault()`（返回 SSLSocketFactory）、`getDefaultCipherSuites`/`getSupportedCipherSuites`、`createSocket(Socket,String,int,boolean)`（分层）、`createSocket(Socket,InputStream,boolean)`。

## 2. Java 用户如何使用
`SSLContext.getSocketFactory().createSocket(plainSocket, host, port, true)` 在既有 socket 上分层 TLS。

## 3. 开源代码实际用法调研
JDK `test/jdk/javax/net/ssl/templates/`；`SSLSocketFactory.getDefault()` 用于 HttpsURLConnection 默认。

## 4. 测试用例设计 + 自动化实现
黑盒：default/getDefaultCipherSuites/getSupportedCipherSuites 非空；分层 socket 握手 + echo。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。
