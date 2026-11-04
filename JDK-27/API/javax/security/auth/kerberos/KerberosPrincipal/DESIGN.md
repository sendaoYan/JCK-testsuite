# KerberosPrincipal 测试设计

## 1. API 功能
`javax.security.auth.kerberos.KerberosPrincipal` 封装 Kerberos 主体名（RFC 1964 / RFC 4120），实现 `java.security.Principal` 与 `Serializable`。

- 构造器：`KerberosPrincipal(String name)`（nameType 默认 `KRB_NT_PRINCIPAL`）、`KerberosPrincipal(String name, int nameType)`。
- 查询：`getName()`（含 realm 的完整名）、`getRealm()`、`getNameType()`。
- 工具/对象方法：`toString()`（等于 getName）、`equals`/`hashCode`（仅基于 getName）。
- 常量：`KRB_NT_UNKNOWN=0`、`KRB_NT_PRINCIPAL=1`、`KRB_NT_SRV_INST=2`、`KRB_NT_SRV_HST=3`、`KRB_NT_SRV_XHST=4`、`KRB_NT_UID=5`、`KRB_NT_ENTERPRISE=10`。

## 2. Java 用户如何使用
用户用带 realm 的字符串构造主体，随后读取名称/realm，或放入 Subject 的 Principal 集合中比较身份：

```java
KerberosPrincipal p = new KerberosPrincipal("alice@EXAMPLE.COM");
String name = p.getName();          // "alice@EXAMPLE.COM"
String realm = p.getRealm();        // "EXAMPLE.COM"
int type = p.getNameType();         // KRB_NT_PRINCIPAL
boolean same = p.equals(new KerberosPrincipal("alice@EXAMPLE.COM"));
```

## 3. 开源代码实际用法调研
- JDK 源码自用（`~/git/jdk25u-dev/src/java.security.jgss/.../kerberos`）：`KerberosKey` 用 `principal.getName()` 作为 keytab 盐；`KerberosTicket` 用 `new KerberosPrincipal(...)` 保存 client/server。
- JDK 自带测试 `~/git/jdk25u-dev/test/jdk/javax/security/auth/kerberos/KerberosNullsAndDestroyTest.java`：`new KerberosPrincipal("me@HERE")`、`new KerberosPrincipal("you@THERE")`，验证 null/destroy 语义。
- JDK 自带测试 `KerberosTixDateTest.java`：`new KerberosPrincipal("client@JLABS.SFBAY.SUN.COM")` 用于构造 ticket。

来源：以上均来自 JDK-27 对应实现与 `test/jdk`，无外部开源库依赖。

## 4. 测试用例设计 + 自动化实现
推导场景（对应 `KerberosPrincipalTest.java` 黑盒部分）：
1. 常量数值逐一断言（7 个）。
2. 单参构造：getName/getRealm/getNameType/toString。
3. 双参构造三种 nameType（PRINCIPAL/SRV_INST/SRV_HST）验证名称与 realm 保留。
4. equals/hashCode：自反、对称、同名单例相等、异名不等、null/非 Principal 不等、hashCode 与 getName().hashCode() 一致。
5. realm 大小写保留且 equals 大小写敏感。
