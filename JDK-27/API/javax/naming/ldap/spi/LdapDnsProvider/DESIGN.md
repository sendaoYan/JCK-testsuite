# javax.naming.ldap.spi.LdapDnsProvider 测试设计

## 1. API 功能
JDK-9+ 服务提供者钩子：LDAP 客户端在建立连接前用 provider 把 `java.naming.provider.url` 的 ldap URL 解析成
(endpoint, domain) 候选集。抽象类、两个 protected 构造器、lookupEndpoints(String url)。

## 2. Java 用户如何使用
LDAP 服务实现方 subclass 并经 META-INF/services 注册；JNDI 层在 InitialLdapContext 构造时自动发现。
测试直接实例化自造子类断言契约，不起网络。

## 3. 开源代码实际用法调研
JDK javax/naming/ldap/spi/LdapDnsProvider.java javadoc；LdapDnsProviderService 内部默认实现。

## 4. 测试用例设计（LdapDnsProviderTest.java）
- 白盒：抽象、lookupEndpoints 唯一抽象方法、protected 构造器存在。
- 黑盒：自造子类返回指定 LdapDnsProviderResult；结果原样透传；result 本身不可变性（endpoints/domain 快照语义）。
