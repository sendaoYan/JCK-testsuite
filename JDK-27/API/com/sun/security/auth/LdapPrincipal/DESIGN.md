# com.sun.security.auth.LdapPrincipal 测试设计案

## 1. API 功能
`LdapPrincipal` 是 RFC 2253 可分辨名（DN）标识的主体，`final` 且实现 `Principal` + `Serializable`。JDK-27 javap 实测：
- 构造器 `LdapPrincipal(String name) throws javax.naming.InvalidNameException`：`null` 抛 NPE，非法 DN 抛 `InvalidNameException`。
- `String getName()`：返回构造时传入的**原始字符串**。
- `String toString()`：返回 `LdapName` 的 RFC 2253 形式。
- `boolean equals(Object)` / `int hashCode()`：按 `LdapName` 语义比较/哈希（对分隔符空格不敏感）。

## 2. Java 用户如何使用
```java
LdapPrincipal p = new LdapPrincipal("cn=foo,dc=example,dc=com");
String raw = p.getName();       // 原始字符串
String dn  = p.toString();      // RFC 2253 形式
LdapPrincipal q = new LdapPrincipal("cn=foo, dc=example, dc=com");
boolean same = p.equals(q);     // true（空格不敏感）
```

## 3. 开源代码实际用法调研
- 实现基准：`~/git/jdk25u-dev/src/jdk.security.auth/share/classes/com/sun/security/auth/LdapPrincipal.java`。
- JDK 回归：`~/git/jdk25u-dev/test/jdk/com/sun/security/auth/CreateLdapPrincipals.java`（`principals.add(new LdapPrincipal("x=y"))` 等非标准属性名）。
（网络不可用，以 JDK 源码 + test/jdk 为权威来源。）

## 4. 测试用例设计 + 自动化实现
黑盒断言在 `LdapPrincipalTest.java` 的 `testBlackBox()`：
- `getName()` 返回原始串；`toString()` 非空；`equals` 自反、空格等价对称、不同 DN 不等、`null`/其他类型不等。
- 不同空格输入 `hashCode` 一致。
- `null` 抛 NPE；`"foo"` 抛 `InvalidNameException`。
- 加入 `Subject` 后可查询。
