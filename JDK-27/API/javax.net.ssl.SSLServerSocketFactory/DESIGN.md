# javax.net.ssl.SSLServerSocketFactory 黑盒行为测试设计

## 1. API 功能
抽象类 extends javax.net.ServerSocketFactory：`getDefault()`、`getDefaultCipherSuites`/`getSupportedCipherSuites`。

## 2. Java 用户如何使用
`SSLContext.getServerSocketFactory().createServerSocket(...)`。

## 3. 开源代码实际用法调研
JDK `test/jdk/javax/net/ssl/templates/`。

## 4. 测试用例设计 + 自动化实现
黑盒：default/getDefaultCipherSuites/getSupportedCipherSuites 非空；createServerSocket 绑定 ephemeral + loopback。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。
