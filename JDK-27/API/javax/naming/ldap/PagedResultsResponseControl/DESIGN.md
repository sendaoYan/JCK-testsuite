# javax.naming.ldap.PagedResultsResponseControl 设计案

## 1. API 功能
`final class extends BasicControl`。构造器：`(String id, boolean criticality, byte[] value)`。常量 `OID = "1.2.840.113556.1.4.319"`。方法：`getResultSize()`、`getCookie()`（空 cookie 返回 null）。解析 BER `SEQUENCE { size INTEGER, cookie OCTET STRING }`。

## 2. Java 用户如何使用
从服务器响应控件构造：`new PagedResultsResponseControl(PagedResultsResponseControl.OID, criticality, value)` → `getResultSize()`/`getCookie()`（cookie 传给下一页请求）。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/ldap/PagedResultsResponseControl.java`。
- 来源：`~/git/jdk25u-dev/test/jdk/com/sun/jndi/ldap` 分页测试。

## 4. 测试用例设计
黑盒断言（PagedResultsResponseControlTest.testBlackBox）：
- 手工 BER（INTEGER 42 + OCTET STRING {1,2,3}）→ getResultSize=42、cookie=[1,2,3]。
- 空 OCTET STRING → getCookie()==null。
- 非关键标志往返。
