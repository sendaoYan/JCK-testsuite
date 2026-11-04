# javax.naming.LinkRef — 测试设计

## 1. API 功能
符号链接引用：extends Reference；ctor(String)/ctor(Name)；`getLinkName()`；单个 StringRefAddr(type="LinkAddress")。

## 2. Java 用户如何使用
`ctx.bind("alias", new LinkRef("real/name"))`；随后 `lookup("alias")` 经由 provider 解析到目标；`((LinkRef) ctx.lookupLink("alias")).getLinkName()` 查看链接本身。

## 3. 开源代码实际用法调研
- `javax.naming.LinkRef` javadoc 场景（Context.lookupLink 配合）。
- `com.sun.jndi.ldap.LdapCtx.lookupLink` 对 LinkRef 的处理路径。

## 4. 测试用例设计（LinkRefTest.java）
- 白盒：extends Reference、两个构造器、getLinkName 声明 NamingException。
- 黑盒：String/Name 构造 round trip；className 恒为 javax.naming.LinkRef；单一地址且 type 为 LinkAddress、内容为链接名。
