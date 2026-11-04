# javax.naming.ldap.spi.LdapDnsProviderResult 设计案

## 1. API 功能
`final class`。构造器：`(String domainName, List<String> endpoints)`。方法：`getDomainName()`（null 域归为空串）、`getEndpoints()`（防御性拷贝、不可变 List）。
- 构造校验：endpoints 含 null 元素抛 `NullPointerException`；含非 String 抛 `ClassCastException`。

## 2. Java 用户如何使用
`new LdapDnsProviderResult("example.com", List.of("ldap://a:389"))` → `getDomainName()/getEndpoints()`。作为 `LdapDnsProvider.lookupEndpoints` 的返回结果。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/ldap/spi/LdapDnsProviderResult.java`（`List.copyOf(endpoints)`、null 域 → ""）。
- 来源：`~/git/jdk25u-dev/test/jdk/com/sun/jndi/ldap/LdapDnsProviderTest.java`。

## 4. 测试用例设计
黑盒断言（LdapDnsProviderResultTest.testBlackBox）：
- domainName/endpoints 往返。
- null 域 → 空串。
- 源 List 变更不影响副本；返回 List 不可变（add 抛 UnsupportedOperationException）。
- 空 endpoints 允许；含 null 元素抛 NullPointerException。
