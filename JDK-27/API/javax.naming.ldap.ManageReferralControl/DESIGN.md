# javax.naming.ldap.ManageReferralControl 设计案

## 1. API 功能
`final class extends BasicControl`。构造器：`ManageReferralControl()`、`(boolean criticality)`。常量 `OID = "2.16.840.1.113730.3.4.2"`。默认关键。

## 2. Java 用户如何使用
`new ManageReferralControl()` 或 `new ManageReferralControl(false)` → `getID()/isCritical()`。告诉 LDAP 服务器按普通条目处理引用（而不是返回引用）。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/ldap/ManageReferralControl.java`。

## 4. 测试用例设计
黑盒断言（ManageReferralControlTest.testBlackBox）：默认/显式关键性、OID、instanceof BasicControl。
