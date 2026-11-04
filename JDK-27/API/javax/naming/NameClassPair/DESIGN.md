# javax.naming.NameClassPair — 测试设计

## 1. API 功能
`Context.list()` 的结果条目：name、className、isRelative、getNameInNamespace；setter 全家。

## 2. Java 用户如何使用
`NamingEnumeration<NameClassPair> e = ctx.list("ou=x"); while (e.hasMore()) { NameClassPair p = e.next(); p.getClassName(); }`。

## 3. 开源代码实际用法调研
- JDK test/jdk/javax/naming/** 的 list 遍历模式。
- `com.sun.jndi.toolkit.ctx.ContextEnumerator` 构造 NameClassPair 子类 Binding。

## 4. 测试用例设计（NameClassPairTest.java）
- 白盒：Serializable、两个构造器、方法清单。
- 黑盒：getter/setter 全覆盖；toString 两种精确形态（相对/非相对，实测 "n: java.lang.String" 与 "(not relative)n: java.lang.String"）；getNameInNamespace 未设置抛 UnsupportedOperationException（实测）。
