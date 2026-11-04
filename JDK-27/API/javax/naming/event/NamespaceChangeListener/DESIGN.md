# javax.naming.event.NamespaceChangeListener — 测试设计

## 1. API 功能
- 继承 `NamingListener`（再继承 `java.util.EventListener`）。
- 回调方法（参数均为 `NamingEvent`）：
  - `objectAdded(NamingEvent)`
  - `objectRemoved(NamingEvent)`
  - `objectRenamed(NamingEvent)`
- 从 `NamingListener` 继承 `namingExceptionThrown(NamingExceptionEvent)`。

## 2. Java 用户如何使用
- 实现该接口（通常同时实现 `ObjectChangeListener`），随 `EventContext.addNamingListener` 注册。
- `NamingEvent.dispatch(listener)` 按事件类型分派到对应回调（`OBJECT_ADDED→objectAdded` 等）。
- 组合链路：构造 `NamingEvent` → `dispatch(namespaceListener)` → 回调记录事件。

## 3. 开源代码实际用法调研
来源：`~/git/jdk25u-dev`。
- `javax/naming/event/NamingEvent.java` 的 `dispatch`：`OBJECT_ADDED/REMOVED/RENAMED` 分别调用 `NamespaceChangeListener.objectAdded/objectRemoved/objectRenamed`。
- `com/sun/jndi/ldap/NamingEventNotifier.java` 为 add/remove/rename 构造 `NamingEvent` 后 `queueEvent` 派发。
- `test/jdk/com/sun/jndi/ldap/RemoveNamingListenerTest.java` 用监听器实现验证命名空间变更通知。

## 4. 测试用例设计
黑盒断言（`NamespaceChangeListenerTest.testBlackBox`）：
- 匿名实现 + `NamingEvent.dispatch`：`OBJECT_ADDED/REMOVED/RENAMED` 各自路由到 `objectAdded/objectRemoved/objectRenamed`，且收到同一 `NamingEvent`。
- `namingExceptionThrown` 直接调用可用。
白盒断言（`testWhiteBox`）：public/interface、继承 `NamingListener`、三个回调方法精确签名。
