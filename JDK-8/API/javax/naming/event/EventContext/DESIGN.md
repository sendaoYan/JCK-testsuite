# javax.naming.event.EventContext — 测试设计

## 1. API 功能
- 作用域常量：`OBJECT_SCOPE=0`、`ONELEVEL_SCOPE=1`、`SUBTREE_SCOPE=2`（编译期常量）。
- 监听器注册/注销（均抛 `NamingException`）：
  - `addNamingListener(Name, int, NamingListener)`
  - `addNamingListener(String, int, NamingListener)`
  - `removeNamingListener(NamingListener)`
- 查询：`boolean targetMustExist()`。
- 接口继承 `javax.naming.Context`。

## 2. Java 用户如何使用
- 实现类（如 LDAP provider 的 `LdapCtx`）持有 `EventContext` 引用注册监听：
  `ctx.addNamingListener("ou=people", EventContext.SUBTREE_SCOPE, listener)`。
- 注销用同一 `EventContext` 实例：`ctx.removeNamingListener(listener)`。
- `targetMustExist()` 查询服务是否要求目标必须已存在。
- 组合链路：`addNamingListener(...)` → 服务端变更 → `listener.objectAdded/NamingEvent` → 用完 `removeNamingListener(...)`。

## 3. 开源代码实际用法调研
来源：`~/git/jdk25u-dev` JDK 自身源码与 `test/jdk`。
- `src/java.naming/share/classes/com/sun/jndi/ldap/EventSupport.java`：LDAP provider 内部实现 `EventContext.addNamingListener`，用 `new NamingEventNotifier(this, ctx, args, l)` 登记监听。
- `test/jdk/com/sun/jndi/ldap/RemoveNamingListenerTest.java`：真实测试导入 `javax.naming.event.EventContext`、`NamingListener`、`NamingEvent`，用 `EventContext` 注册并验证移除监听后不再收到事件。
- 作用域常量被 `com.sun.jndi.ldap.LdapCtx` 的 `addNamingListener` 实现读取，用于决定搜索范围。

## 4. 测试用例设计
黑盒断言（`EventContextTest.testBlackBox`）：
- 三个作用域常量精确值 `0/1/2`（规范明确）。
- 用动态代理实例化 `EventContext`，验证 `targetMustExist()` 可调用、`addNamingListener(Name/String, scope, listener)` 与 `removeNamingListener(listener)` 均可调用并触发。
白盒断言（`testWhiteBox`）：public/interface/无超类、继承 `Context`、四个方法签名及 `targetMustExist` 返回 `boolean`。
