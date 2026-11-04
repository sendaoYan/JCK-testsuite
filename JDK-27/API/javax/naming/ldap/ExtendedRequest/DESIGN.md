# javax.naming.ldap.ExtendedRequest 设计案

## 1. API 功能
接口（`extends Serializable`）。方法：`getID()`、`getEncodedValue()`、`createExtendedResponse(String id, byte[] berValue, int offset, int length)`。

## 2. Java 用户如何使用
LDAP 扩展操作请求抽象。典型：`StartTlsRequest` 实现该接口；`getID()` 返回扩展 OID、`getEncodedValue()` 返回请求值（可为 null）、`createExtendedResponse(...)` 构造对应响应。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/ldap/ExtendedRequest.java`。
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/ldap/StartTlsRequest.java`（实现该接口）。

## 4. 测试用例设计
白盒（ExtendedRequestTest.testWhiteBox）：public/接口/Serializable + 方法。
黑盒（testBlackBox）：匿名实现验证 getID/getEncodedValue/createExtendedResponse 可调用。
