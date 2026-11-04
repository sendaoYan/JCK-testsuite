# javax.naming.ldap.spi.LdapDnsProvider 设计案

## 1. API 功能
抽象类。方法：`lookupEndpoints(String url, Map<?,?> env)` 返回 `Optional<LdapDnsProviderResult>`（throws `NamingException`）。

## 2. Java 用户如何使用
服务提供者子类化并实现 DNS → LDAP 端点解析；典型：`lookupEndpoints("ldap://example.com", env)` → `Optional.of(new LdapDnsProviderResult("example.com", List.of("ldap://a:389")))`。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/test/jdk/com/sun/jndi/ldap/LdapDnsProviderTest.java`（通过 `META-INF/services/javax.naming.ldap.spi.LdapDnsProvider` 注册与 `lookupEndpoints` 调用）。
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/ldap/spi/LdapDnsProvider.java`。

## 4. 测试用例设计
白盒（LdapDnsProviderTest.testWhiteBox）：public/abstract + lookupEndpoints。
黑盒（testBlackBox）：匿名子类实现 lookupEndpoints 返回 `Optional.of(result)`，验证 present/identity/domain。
