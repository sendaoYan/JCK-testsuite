# javax.net.ssl.SSLServerSocket 黑盒行为测试设计

## 1. API 功能
抽象类 extends java.net.ServerSocket：get/setEnabledCipherSuites、getSupportedCipherSuites、get/setEnabledProtocols、getSupportedProtocols、set/getNeedClientAuth、set/getWantClientAuth、set/getUseClientMode、set/getEnableSessionCreation、get/setSSLParameters。

## 2. Java 用户如何使用
`SSLServerSocketFactory.createServerSocket(0, backlog, loopback)` 绑定后 `accept()`。

## 3. 开源代码实际用法调研
JDK `test/jdk/javax/net/ssl/SSLServerSocket/` 覆盖。

## 4. 测试用例设计 + 自动化实现
黑盒：server 模式默认、auth 默认 false、TLSv1.3 enabled；need/want 互斥；useClientMode 可切换；无效 cipher/protocol IAE；accept 握手 echo。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。
