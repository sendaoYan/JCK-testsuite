# javax.naming.ldap.BasicControl 设计案

## 1. API 功能
`implements javax.naming.ldap.Control`。
- 构造器：`BasicControl(String id)`、`(String id, boolean criticality, byte[] value)`。
- 查询：`getID()`、`isCritical()`、`getEncodedValue()`。

## 2. Java 用户如何使用
典型：`new BasicControl("1.2.3", true, new byte[]{1,2})` → `getID()/isCritical()/getEncodedValue()`。单参构造默认非关键、无编码值。作为 LDAP 控件的通用实现基类。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/ldap/BasicControl.java`。
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/ldap/PagedResultsControl.java` 等以 BasicControl 为基类。

## 4. 测试用例设计
黑盒断言（BasicControlTest.testBlackBox）：单参构造默认（id、非关键、编码 null）；三参构造（id/critical/编码值往返）。
