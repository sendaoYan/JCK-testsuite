# javax.naming.event.ObjectChangeListener — 测试设计

## 1. API 功能
- 继承 `NamingListener`。
- 回调：`objectChanged(NamingEvent)`。
- 从 `NamingListener` 继承 `namingExceptionThrown(NamingExceptionEvent)`。

## 2. Java 用户如何使用
- 实现对 `NamingEvent.OBJECT_CHANGED` 类型感兴趣的监听器并随 `EventContext` 注册。
- `NamingEvent.dispatch` 对 `OBJECT_CHANGED` 调用 `((ObjectChangeListener)listener).objectChanged(this)`。
- 组合链路：`new NamingEvent(src, OBJECT_CHANGED, newBd, oldBd, info)` → `dispatch(listener)` → `listener.objectChanged(evt)`。

## 3. 开源代码实际用法调研
来源：`~/git/jdk25u-dev`。
- `javax/naming/event/NamingEvent.java` 的 `dispatch`：`case OBJECT_CHANGED: ((ObjectChangeListener)listener).objectChanged(this);`
- `com/sun/jndi/ldap/NamingEventNotifier.fireObjectChanged` 用 `OBJECT_CHANGED` 构造 `NamingEvent` 派发。
- `test/jdk/com/sun/jndi/ldap/RemoveNamingListenerTest.java` 导入并使用 `ObjectChangeListener`。

## 4. 测试用例设计
黑盒断言（`ObjectChangeListenerTest.testBlackBox`）：
- `NamingEvent.OBJECT_CHANGED` 经 `dispatch` 路由到 `objectChanged` 且收到同一事件。
- 直接调用 `objectChanged` 可用；继承的 `namingExceptionThrown` 可用。
白盒断言（`testWhiteBox`）：public/interface、继承 `NamingListener`、方法签名。
