# javax.naming.event.EventDirContext — 测试设计

## 1. API 功能
- 继承 `EventContext` 与 `javax.naming.directory.DirContext`。
- 四个带搜索过滤的注册重载（均抛 `NamingException`）：
  - `addNamingListener(Name, String filter, SearchControls, NamingListener)`
  - `addNamingListener(String, String filter, SearchControls, NamingListener)`
  - `addNamingListener(Name, String filter, Object[] filterArgs, SearchControls, NamingListener)`
  - `addNamingListener(String, String filter, Object[] filterArgs, SearchControls, NamingListener)`

## 2. Java 用户如何使用
- 目录服务注册按过滤器触发的变更监听：
  `edc.addNamingListener("ou=people", "(uid=*)", ctls, listener)`。
- 带参数的过滤器：`edc.addNamingListener(name, "(&(uid={0})(ou={1}))", new Object[]{"jsmith","dev"}, ctls, listener)`。
- `SearchControls` 指定搜索作用域/返回属性，过滤器决定哪些变更触发事件。
- 组合链路：`new SearchControls()` → `addNamingListener(...)` → 目录变更 → 监听器回调。

## 3. 开源代码实际用法调研
来源：`~/git/jdk25u-dev`。
- `com/sun/jndi/ldap/EventSupport.java` 的 `addNamingListener` 分支处理带 `SearchControls` 的重载，构造 `NamingEventNotifier`。
- `com/sun/jndi/ldap/EventQueue`/`NamingEventNotifier` 在过滤器匹配时用 `NamingEvent` 通知 `NamespaceChangeListener`/`ObjectChangeListener`。
- `test/jdk/com/sun/jndi/ldap/RemoveNamingListenerTest.java` 覆盖目录监听注册/注销生命周期。

## 4. 测试用例设计
黑盒断言（`EventDirContextTest.testBlackBox`）：
- 继承的作用域常量 `OBJECT_SCOPE==0`、`SUBTREE_SCOPE==2`。
- 用动态代理实例化 `EventDirContext`，验证两个搜索式 `addNamingListener`（带/不带 `filterArgs`）均可调用并计数触发。
白盒断言（`testWhiteBox`）：public/interface、双继承（EventContext + DirContext）、四个重载精确签名。
