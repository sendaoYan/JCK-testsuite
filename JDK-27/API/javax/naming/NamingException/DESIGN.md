# javax.naming.NamingException — 测试设计

## 1. API 功能
JNDI 命名异常的基类（extends Exception）：`()`/`(String)` 构造器；resolved/remaining name 与 resolved obj
（`setResolvedName/getResolvedName`、`setRemainingName/getRemainingName`、`setResolvedObj/getResolvedObj`）；
`appendRemainingComponent(String)`、`appendRemainingName(Name)`；root cause（`setRootCause/getRootCause`）与
`getCause/initCause` 桥接；`getExplanation()`、`toString()` 与 `toString(boolean detail)`。

## 2. Java 用户如何使用
`try { ctx.lookup(n); } catch (NamingException e) { e.getRemainingName(); e.getRootCause(); }`。
提供方 throw 时填充 resolved/remaining name 与 root cause，客户端用 `appendRemainingComponent` 沿解析路径回溯名字。

## 3. 开源代码实际用法调研
- `com.sun.jndi.toolkit.ctx.PartialCompositeContext`、`LdapCtx` 大量 `setRemainingName/setResolvedName`。
- JDK 测试 `test/jdk/javax/naming/**` 验证 toString 与 root cause 组成。
- Spring `NamingExceptionUtils`、Tomcat `NamingContextBindingsEnumeration` 读取 explanation/root cause。

## 4. 测试用例设计（NamingExceptionTest.java）
- 白盒：extends Exception、方法清单、toString(boolean)、双构造器。
- 黑盒：message/explanation 一致性；remaining/resolved name/obj 设置与 clone 语义、置 null 清空；
  appendRemainingComponent/Name 链式拼接；root cause 与 Throwable cause 的镜像、initCause 二次调用抛 ISE；
  toString 组成（header/root exception/remaining name）、toString(false) 保留 message。
