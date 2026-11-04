# javax.naming.InsufficientResourcesException — 测试设计

## 1. API 功能
JNDI 命名异常族成员：继承自 `javax.naming.NamingException`（最终追溯到 `NamingException`），
提供 `()` 与 `(String explanation)` 两个构造器；继承 NamingException 的 resolved/remaining name、resolved obj、
root cause、`appendRemainingComponent/Name`、`toString` 等全套状态机制。

## 2. Java 用户如何使用
服务提供方在 lookup/bind/search 失败时抛出对应语义的异常；客户端按子类捕获细分处理，或按 `NamingException` 统一捕获；
`e.getExplanation()` 取人类可读原因、`e.getRemainingName()` 取解析到一半的名字、`e.getRootCause()` 取底层异常。

## 3. 开源代码实际用法调研
- JDK `java.naming` 源码：`LdapCtx`、`InitialContext`、`NamingManager` 在失败路径 throw 这些子类
  （如 `NoInitialContextException`、`NameNotFoundException`、`CannotProceedException`）。
- JDK 测试 `test/jdk/javax/naming/**` 大量 `try/catch(NameNotFoundException)` 断言查找失败。
- Spring `org.springframework.jndi.JndiTemplate`、Tomcat JNDI 客户端均依赖 NamingException 族做错误分支。

## 4. 测试用例设计（InsufficientResourcesExceptionTest.java）
- 白盒：public、继承链（extends NamingException）、Serializable、`()`/`(String)` 两个构造器。
- 黑盒：message/explanation 保留；no-arg message null；继承的 remaining/resolved name、resolved obj 往返；
  toString 含类名与 message；可被父类/NamingException 捕获；对部分类断言真实生产路径的精确 message
  （如 InvalidNameException 由 CompositeName 尾部转义产生、NoInitialContextException 由无工厂 lookup 产生）。
