# javax.naming.Context — 测试设计

## 1. API 功能
JNDI 命名上下文根接口：lookup/bind/rebind/unbind/rename、list/listBindings、createSubcontext/destroySubcontext、
lookupLink、getNameParser、composeName、addToEnvironment/removeFromEnvironment/getEnvironment、
getNameInNamespace、close；全部操作提供 String 与 Name 两套重载；附带 14 个环境属性常量。

## 2. Java 用户如何使用
`Context ctx = new InitialContext(); Object o = ctx.lookup("jdbc/ds"); ctx.bind("x", obj);`
层级遍历 `listBindings`；命名空间合成 `composeName`；环境属性由 `Context.INITIAL_CONTEXT_FACTORY` 等常量引用。

## 3. 开源代码实际用法调研
- JDK `InitialContext` 实现 Context 全部操作；`com.sun.jndi.toolkit.ctx` 通用上下文实现。
- Spring `JndiTemplate`、Tomcat `NamingContext` 均以 Context 接口为统一入口。
- JDK test/jdk/javax/naming/** 的 lookup/bind/list 组合。

## 4. 测试用例设计（ContextTest.java）
- 白盒：接口、17 个操作族、Name 重载、list 返回 NamingEnumeration、lookup 声明 NamingException。
- 黑盒：14 个环境常量精确值；用内存 FlatContext 实现端到端 bind→lookup→rebind→rename→list（排序）→
  listBindings→unbind→addToEnvironment/removeFromEnvironment→composeName→close。
