# javax.naming.ldap.StartTlsResponse 设计案

## 1. API 功能
抽象类（`implements ExtendedResponse`）。常量 `OID = "1.3.6.1.4.1.1466.20037"`。
- 具体：`getID()`（返回 OID）、`getEncodedValue()`（返回 null）。
- 抽象：`setEnabledCipherSuites(String[])`、`setHostnameVerifier(HostnameVerifier)`、`negotiate()`、`negotiate(SSLSocketFactory)`、`close()`。

## 2. Java 用户如何使用
由 StartTLS 扩展响应构造；用户配置密码套件/主机名校验后调用 `negotiate()` 建立 TLS 会话，用毕 `close()`。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/ldap/StartTlsResponse.java`。
- 来源：JDK 内部 `com.sun.jndi.ldap.ext.StartTlsResponseImpl` 实现。

## 4. 测试用例设计
白盒（StartTlsResponseTest.testWhiteBox）：public/abstract/implements ExtendedResponse + 方法。
黑盒（testBlackBox）：匿名子类实现抽象方法，验证 getID==OID、getEncodedValue==null、配置与 negotiate/close 可调用。
