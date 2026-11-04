# javax.naming.ldap.Rdn 设计案

## 1. API 功能
`implements Serializable, Comparable<Object>`，RFC 2253 相对可分辨名。
- 构造器：`Rdn(String)`、`Rdn(Attributes)`、`Rdn(Rdn)`、`Rdn(String type, Object value)`。
- 查询：`getType()`、`getValue()`、`size()`、`toString()`、`toAttributes()`。
- 工具：`compareTo`、`equals`、`hashCode`。
- 静态：`escapeValue(Object)`、`unescapeValue(String)`。

## 2. Java 用户如何使用
典型：`new Rdn("cn=John+sn=Doe")`（多值）→ `getType()`（首类型）/`getValue()`/`size()`/`toAttributes()`。`Rdn(String,Object)` 自动转义值（如 `a,b+c` → `a\,b\+c`）。`escapeValue`/`unescapeValue` 用于手动处理转义。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/test/jdk/javax/naming/ldap/LdapName/RdnMisc.java`（`new Rdn(attrs)`、`rdn.toAttributes()`、`new Rdn("cn=Juicy\\, Fruit")`）。
- 来源：`~/git/jdk25u-dev/test/jdk/javax/naming/ldap/LdapName/RdnToAttrsTest.java`。
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/ldap/Rdn.java`。

## 4. 测试用例设计
黑盒断言（RdnTest.testBlackBox）：
- 单值/多值解析（type/value/size/toString）。
- `Rdn(String,Object)` 转义、`Rdn(Rdn)` 复制。
- `toAttributes`（id→value）。
- `escapeValue`/`unescapeValue` 往返。
- equals/hashCode/compareTo（相等为 0、`cn=a`<`cn=b`）。
- 非法 `new Rdn("=x")` 抛 `InvalidNameException`。
