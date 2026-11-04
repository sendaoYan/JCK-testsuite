# javax.naming.CompositeName — 测试设计

## 1. API 功能
跨命名系统复合名的具体实现：`CompositeName(String)` 解析（'/' 分隔、'\' 转义、'"' 引用）、
无参构造空名、全部 Name 操作（add/remove/prefix/suffix/startsWith/endsWith/compareTo/clone）、
equals/hashCode、Serializable。

## 2. Java 用户如何使用
`new CompositeName("cn=home,dc=x/y")`；URL 前后缀切分 `name.getSuffix(1)`；把解析结果逐组件 add 回去；
`ctx.rename(name, name.add("leaf"))`。链路：parse → escape/quote 校验 → mutate → match → compare → clone → serialize。

## 3. 开源代码实际用法调研
- `com/sun/jndi/ldap/LdapNamingEnumeration.java:59` 用 `new CompositeName()` 组装 URL 名。
- `com/sun/jndi/url/ldap/ldapURLContextFactory.java:64` 用 `new CompositeName()` 拆 URL 剩余部分。
- JDK 测试 `test/jdk/javax/naming/Name/Serialized13Name.java` 做序列化兼容；OpenJDK test/jdk/javax/naming/** 多处 parse+toString 往返。

## 4. 测试用例设计（CompositeNameTest.java）
- 白盒：public、实现 Name、Serializable、String 构造器声明 InvalidNameException、16 个方法存在。
- 黑盒：空串/空构造；转义分隔符 `a\/b`（1 组件）与 toString 重引号；引用组件；尾部转义与未闭合引用的
  **精确 message**（实测稳定："a\\: unescaped \\ at end of component"、"\"x: no close quote"）；
  add/add(int)/addAll/addAll(int)/remove 链（断言 size/get 全程）；0/满长度 prefix/suffix；
  equals/hashCode 契约、compareTo（含 String 参数 CCE "Not a CompositeName"）；clone 独立性；序列化往返。
