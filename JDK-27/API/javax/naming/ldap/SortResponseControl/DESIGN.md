# javax.naming.ldap.SortResponseControl 设计案

## 1. API 功能
`final class extends BasicControl`。构造器：`(String id, boolean criticality, byte[] value)`。常量 `OID = "1.2.840.113556.1.4.474"`。方法：`isSorted()`、`getResultCode()`、`getAttributeID()`、`getException()`。解析 BER `SEQUENCE { ENUMERATED sortResult, [0] AttributeType OPTIONAL }`。

## 2. Java 用户如何使用
从响应构造：`new SortResponseControl(SortResponseControl.OID, criticality, value)` → `isSorted()`/`getResultCode()`/`getAttributeID()`/`getException()`。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/ldap/SortResponseControl.java`（`getException()` 经 `LdapCtx.mapErrorCode` 映射）。

## 4. 测试用例设计
黑盒断言（SortResponseControlTest.testBlackBox）：
- 成功 BER（ENUMERATED 0）→ isSorted true、code 0、attr null、exception null。
- 失败 BER（ENUMERATED 16 + [0] "cn"）→ isSorted false、code 16、attr "cn"、exception `NoSuchAttributeException`。
