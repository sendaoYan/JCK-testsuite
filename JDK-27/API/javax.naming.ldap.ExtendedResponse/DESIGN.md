# javax.naming.ldap.ExtendedResponse 设计案

## 1. API 功能
接口（`extends Serializable`）。方法：`getID()`、`getEncodedValue()`。

## 2. Java 用户如何使用
LDAP 扩展操作响应抽象。典型：`StartTlsResponse` 实现该接口；`getID()` 返回 OID、`getEncodedValue()` 返回响应值（可为 null）。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/ldap/ExtendedResponse.java`。
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/ldap/StartTlsResponse.java`（实现该接口）。

## 4. 测试用例设计
白盒（ExtendedResponseTest.testWhiteBox）：public/接口/Serializable + 方法。
黑盒（testBlackBox）：匿名实现验证 getID/getEncodedValue 可调用。
