# javax.naming.InitialContext — 测试设计

## 1. API 功能
JNDI 命名操作的入口门面（implements Context）：`()`/`(Hashtable)` 公开构造器、protected `(boolean lazy)`；
`doLookup`（静态）；protected `getDefaultInitCtx/getURLOrDefaultInitCtx`；环境管理 `addToEnvironment/
removeFromEnvironment/getEnvironment`；`composeName` 固定语义（初始上下文无父名）；`close` 释放默认上下文。

## 2. Java 用户如何使用
`InitialContext ic = new InitialContext(env); ic.lookup("jdbc/ds");` 或 `InitialContext.doLookup("java:comp/env/x")`；
无 factory 时构造不抛、首个需要默认上下文的操作抛 NoInitialContextException。

## 3. 开源代码实际用法调研
- JDK `InitialContext` 源码：构造时 clone 环境、`init()` 惰性创建默认上下文；JDK 测试 `test/jdk/javax/naming/InitialContext/EnvClone.java`
  验证环境 clone 语义。
- Spring `JndiTemplate.getContext` 用 `new InitialContext(env)`；Tomcat 数据源 JNDI 查找。

## 4. 测试用例设计（InitialContextTest.java）
- 白盒：public、implements Context、protected lazy ctor、public 双构造器、doLookup 静态、protected 方法清单。
- 黑盒：Phase 1（无工厂）构造不抛、lookup/list/doLookup 抛 NoInitialContextException 精确 message、伪造工厂类名抛
  "Cannot instantiate class: ..." + root cause CNFE；Phase 2（安装确定性工厂 builder）bind/rebind/rename/unbind/lookup/
  list/listBindings/createSubcontext/destroySubcontext/lookupLink/getNameInNamespace/composeName/getNameParser、
  环境 clone 语义与 addToEnvironment/removeFromEnvironment、close。
