# javax.naming.Name — 测试设计

## 1. API 功能
`Name` 是 JNDI 名字的顶层接口（分层/扁平名的抽象）：`size/isEmpty/get/getAll`（查询）、
`getPrefix/getSuffix`（切分）、`startsWith/endsWith`（匹配）、`add/addAll/remove`（构造性修改，返回 this）、
`clone`、`compareTo`（Comparable）、序列化。

## 2. Java 用户如何使用
用户几乎总是拿到 `Context.list()/lookup()` 返回的 Name，或用 `new CompositeName("a/b/c")` 构造后
以 `Name` 类型多态使用：`Name n = ctx.getParser().parse(s); n = n.add("d"); obj = ctx.lookup(n);`。
典型链路：parse → add/addAll 组合 → getPrefix/getSuffix → startsWith → remove → compareTo → clone。

## 3. 开源代码实际用法调研
- JDK 内部 `com.sun.jndi.ldap.LdapAttribute`、`com/sun/jndi/toolkit/ctx/AtomicContext.java` 大量以 `Name` 接口传参与切分（`getPrefix/getSuffix`）。
- JDK 测试 `test/jdk/javax/naming/Name/Serialized13Name.java` 验证 Name 序列化兼容。
- 目录服务客户端（如 Apache Harmony JNDI、Tomcat JNDI 查找）均以 `Name` 接口 + CompositeName 实现使用 `size/get/add`。

## 4. 测试用例设计（NameTest.java）
- 白盒：接口形态、Cloneable/Serializable/Comparable、14 个方法签名（返回类型）。
- 黑盒：通过 CompositeName 实现（多态）：size/get/getAll 枚举顺序、prefix/suffix、startsWith/endsWith、
  add/add(int)/addAll/addAll(int) 组合链、remove、compareTo（等/序）、clone 独立性、
  非法尾部转义 → InvalidNameException。
