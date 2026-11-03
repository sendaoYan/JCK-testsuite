# javax.naming.NamingEnumeration — 测试设计

## 1. API 功能
JNDI 专用枚举：`next()/hasMore()` 可抛 NamingException，`close()` 释放服务端资源；兼容
`java.util.Enumeration`（`nextElement/hasMoreElements`）。

## 2. Java 用户如何使用
`NamingEnumeration<NameClassPair> en = ctx.list("ou=people"); while (en.hasMore()) { en.next(); } en.close();`
遍历搜索结果 `NamingEnumeration<SearchResult>` 后必须 close（LDAP 场景释放连接游标）。

## 3. 开源代码实际用法调研
- `com.sun.jndi.ldap.LdapNamingEnumeration` 实现并管理请求游标与 close 语义。
- JDK test/jdk/javax/naming/ldap/... 遍历 SearchResult 枚举的标准范式。
- Spring LDAP `LdapTemplate.search` 回调内部同样消费 NamingEnumeration 并 close。

## 4. 测试用例设计（NamingEnumerationTest.java）
- 白盒：接口、extends Enumeration、next/hasMore/close 签名与 NamingException 声明。
- 黑盒：ListEnum 实现：hasMore/next 消费顺序、耗尽后 next 抛 NoSuchElementException、close 后 hasMore 抛
  NamingException、legacy 方法桥接、迭代求和范式。
