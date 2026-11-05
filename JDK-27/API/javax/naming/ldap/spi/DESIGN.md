# javax.naming.ldap.spi 包 测试设计案

> JDK-9+ 新增子包（JDK-8 无）。headless 安全，不起网络。

## 1. API 功能
LDAP DNS 解析插拔点：LdapDnsProvider（抽象、lookupEndpoints(String url)）与 LdapDnsProviderResult
（endpoints + domain 值对象）。

## 2. Java 用户如何使用
LDAP 服务实现方 subclass LdapDnsProvider 并经 META-INF/services 注册；JNDI 在 InitialLdapContext
建连前调用 lookupEndpoints 获得 (endpoint, domain) 候选。

## 3. 参考来源
JDK javax/naming/ldap/spi javadoc；内部 LdapDnsProviderService 默认实现。

## 4. 测试用例设计
见 LdapDnsProvider/ 与 LdapDnsProviderResult/ 子目录：自造子类闭环、构造器校验、快照语义。
