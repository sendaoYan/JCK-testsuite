# javax.naming.ldap.Control 设计案

## 1. API 功能
接口（`extends Serializable`）。方法：`getID()`、`isCritical()`、`getEncodedValue()`。常量：`CRITICAL=true`、`NONCRITICAL=false`。

## 2. Java 用户如何使用
LDAP 请求/响应控件的抽象。典型：自定义类或 `BasicControl` 实现，`getID()` 返回 OID、`isCritical()` 返回关键性、`getEncodedValue()` 返回 BER 值。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/ldap/Control.java`。
- 来源：`~/git/jdk25u-dev/test/jdk/javax/naming/module/src/authz/.../AuthzIdResponseControlFactory.java` 使用 `control.isCritical()`、`control.getID()`。

## 4. 测试用例设计
白盒（ControlTest.testWhiteBox）：public/接口/Serializable + 常量 + 方法存在性。
黑盒（testBlackBox）：匿名实现验证 getID/isCritical/getEncodedValue 可调用。
