# javax.naming.ldap.spi.LdapDnsProviderResult 测试设计

## 1. API 功能
LdapDnsProvider.lookupEndpoints 的返回值：endpoint 列表 + domain 名。JDK-9+ 新类型（JDK-8 无此类）。

## 2. Java 用户如何使用
`new LdapDnsProviderResult("example.com", List.of("ldap://dc1.example.com:389"))`；
provider 实现返回它，JNDI 内部据此建连。

## 3. 开源代码实际用法调研
JDK javax/naming/ldap/spi/LdapDnsProviderResult.java：构造器 null/空参校验与防御拷贝语义。

## 4. 测试用例设计（LdapDnsProviderResultTest.java）
- 白盒：final 类、公共构造器形态。
- 黑盒：getDomainName/getEndpoints 往返；null 参数抛 NullPointerException（JDK-9+ Objects.requireNonNull 风格）；
  endpoints 视图不被外部修改影响（防御拷贝/不可变列表语义，按实际 javap 双侧断言——JDK-8 侧无此类型，用存在性反射跳过）。
