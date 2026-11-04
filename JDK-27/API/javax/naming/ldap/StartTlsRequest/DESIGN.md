# javax.naming.ldap.StartTlsRequest 设计案

## 1. API 功能
`implements ExtendedRequest`。构造器：`StartTlsRequest()`。常量 `OID = "1.3.6.1.4.1.1466.20037"`。方法：`getID()`、`getEncodedValue()`（返回 null）、`createExtendedResponse(String, byte[], int, int)`（返回 `StartTlsResponse`）。

## 2. Java 用户如何使用
典型：`extendedOperation(new StartTlsRequest())` 启动 LDAP StartTLS；随后 `createExtendedResponse(...)` 得到 `StartTlsResponse` 进行 TLS 协商。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/ldap/StartTlsRequest.java`。
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/ldap/StartTlsResponse.java`。

## 4. 测试用例设计
黑盒断言（StartTlsRequestTest.testBlackBox）：getID==OID、getEncodedValue==null、createExtendedResponse 返回 StartTlsResponse 且其 getID==OID。
