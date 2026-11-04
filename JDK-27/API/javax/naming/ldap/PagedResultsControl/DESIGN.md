# javax.naming.ldap.PagedResultsControl 设计案

## 1. API 功能
`final class extends BasicControl`。构造器：`(int pageSize, boolean criticality)`、`(int pageSize, byte[] cookie, boolean criticality)`。常量 `OID = "1.2.840.113556.1.4.319"`。请求值为 BER `SEQUENCE { size INTEGER, cookie OCTET STRING }`（RFC 2696）。

## 2. Java 用户如何使用
典型：`new PagedResultsControl(20, Control.CRITICAL)` 加入请求控件；后续页 `new PagedResultsControl(pageSize, cookie, criticality)`。响应由 `PagedResultsResponseControl` 解析。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/ldap/PagedResultsControl.java`（文档示例分页）。
- 来源：`~/git/jdk25u-dev/test/jdk/com/sun/jndi/ldap` 分页测试。

## 4. 测试用例设计
黑盒断言（PagedResultsControlTest.testBlackBox）：
- OID/关键性/编码值非空。
- 与 `PagedResultsResponseControl` 往返：编码值解码得相同 pageSize 与 cookie。
- null cookie 归一为空 cookie（往返 size 正确、cookie 为 null）。
