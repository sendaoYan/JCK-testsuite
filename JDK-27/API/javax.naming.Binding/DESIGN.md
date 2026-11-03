# javax.naming.Binding — 测试设计

## 1. API 功能
`Context.listBindings()` 结果条目：extends NameClassPair + 对象本体 `getObject/setObject`；4 个构造器。

## 2. Java 用户如何使用
`for (Binding b : list) { if (b.getObject() instanceof Context) recurse(b.getName()); }`——层级遍历的标准写法。

## 3. 开源代码实际用法调研
- JDK test/jdk/javax/naming/** listBindings 遍历。
- Apache Harmony JNDI 与 Spring 的 JndiTemplate.listBindings 使用 getObject 驱动遍历。

## 4. 测试用例设计（BindingTest.java）
- 白盒：extends NameClassPair、4 构造器、getObject/setObject。
- 黑盒：两参构造（className 从对象推导）、setObject 更新 className、null 对象 → null className、显式 className、isRelative + toString 精确格式（实测 "(not relative)n: java.lang.String:val"）。
