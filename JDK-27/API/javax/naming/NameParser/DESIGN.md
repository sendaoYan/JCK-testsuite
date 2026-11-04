# javax.naming.NameParser — 测试设计

## 1. API 功能
单方法接口：`Name parse(String)` throws NamingException。每个命名系统一个解析器，由 `Context.getNameParser()` 提供。

## 2. Java 用户如何使用
`NameParser p = ctx.getNameParser(""); Name n = p.parse("a.b.c"); n.add("newLeaf"); ctx.bind(n, obj);`
解析器复用使 Name 与字符串形式可互转。

## 3. 开源代码实际用法调研
- `com.sun.jndi.ldap.LdapCtx.getNameParser` 返回内部解析器解析 DN；Spring LDAP 的 `LdapNameParser` 同模式。
- JDK test/jdk/javax/naming/spi/NamingManager/** 用自定义 NameParser 验证 Context 对接。

## 4. 测试用例设计（NameParserTest.java）
- 白盒：接口、parse(String) 返回 Name、声明 NamingException。
- 黑盒：自实现 DotParser（split '.' → CompositeName）：解析大小/组件/确定性（同输入等结果）、空输入、
  调用计数；另一个抛 InvalidNameException 的解析器验证异常透传（精确 message）。
