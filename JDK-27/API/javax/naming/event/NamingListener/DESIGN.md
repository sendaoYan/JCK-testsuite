# javax.naming.event.NamingListener — 测试设计

## 1. API 功能
- 继承 `java.util.EventListener`。
- 唯一回调：`namingExceptionThrown(NamingExceptionEvent)`。

## 2. Java 用户如何使用
- 作为命名监听器家族（`NamespaceChangeListener`、`ObjectChangeListener`）的根接口，统一被 `EventContext.addNamingListener` 接受。
- 组合链路：`NamingExceptionEvent.dispatch(listener)` → `listener.namingExceptionThrown(evt)`。
- 监听器需实现该方法以接收服务端的异常通知。

## 3. 开源代码实际用法调研
来源：`~/git/jdk25u-dev`。
- `javax/naming/event/NamingExceptionEvent.java` 的 `dispatch` 调用 `listener.namingExceptionThrown(this)`。
- `test/jdk/com/sun/jndi/ldap/RemoveNamingListenerTest.java` 的监听器实现 `NamingListener`（或其子接口）并实现 `namingExceptionThrown`。
- `com/sun/jndi/ldap/EventSupport.java` 用 `NamingListener` 引用统一注册/派发。

## 4. 测试用例设计
黑盒断言（`NamingListenerTest.testBlackBox`）：
- 匿名实现直接调用 `namingExceptionThrown` 可用。
- `NamingExceptionEvent.dispatch` 调用该回调并送达同一事件。
- 实现可当作 `java.util.EventListener` 引用使用。
白盒断言（`testWhiteBox`）：public/interface、继承 `EventListener`、方法签名。
