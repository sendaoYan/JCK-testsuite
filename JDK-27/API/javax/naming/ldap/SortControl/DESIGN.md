# javax.naming.ldap.SortControl 设计案

## 1. API 功能
`final class extends BasicControl`。构造器：`(String attrID, boolean criticality)`、`(String[] attrIDs, boolean criticality)`、`(SortKey[] keys, boolean criticality)`。常量 `OID = "1.2.840.113556.1.4.473"`（RFC 2891 服务端排序）。

## 2. Java 用户如何使用
典型：`new SortControl(new SortKey[]{new SortKey("cn", true, "caseIgnoreMatch")}, Control.CRITICAL)` → 加入请求控件，服务端按指定键排序；响应由 `SortResponseControl` 解析。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/ldap/SortControl.java`。
- 来源：`~/git/jdk25u-dev/test/jdk/com/sun/jndi/ldap` 排序测试。

## 4. 测试用例设计
黑盒断言（SortControlTest.testBlackBox）：三种构造器的 OID/关键性/编码值非空。
