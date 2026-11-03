# javax.naming.event.NamingExceptionEvent — 测试设计

## 1. API 功能
- 继承 `java.util.EventObject`。
- 构造器：`NamingExceptionEvent(EventContext source, NamingException exc)`。
- 查询：`getException()`、`getEventContext()`。
- 派发：`dispatch(NamingListener)` 调用 `listener.namingExceptionThrown(this)`。

## 2. Java 用户如何使用
- 服务在收集通知过程中遇到 `NamingException` 时包装为事件通知监听器。
- 典型构造：`new NamingExceptionEvent(eventSrc, e)`（见 JDK `NamingEventNotifier.fireNamingException`）。
- 组合链路：`new NamingExceptionEvent(src, exc)` → `evt.dispatch(listener)` → `listener.namingExceptionThrown(evt)` → `getException()`。

## 3. 开源代码实际用法调研
来源：`~/git/jdk25u-dev`。
- `com/sun/jndi/ldap/NamingEventNotifier.java`：`NamingExceptionEvent evt = new NamingExceptionEvent(eventSrc, e); support.queueEvent(evt, namingListeners);`
- `com/sun/jndi/ldap/EventSupport.java`：`new NamingExceptionEvent(ctx, (NamingException) obj)` 用于把读取阶段异常转给监听器。
- `javax/naming/event/NamingExceptionEvent.java` 的 `dispatch` 直接调用 `listener.namingExceptionThrown(this)`。

## 4. 测试用例设计
黑盒断言（`NamingExceptionEventTest.testBlackBox`）：
- `getException()` 返回构造传入的同一异常实例、`getEventContext()/getSource()` 返回同一源。
- `dispatch` 把同一事件送达 `namingExceptionThrown`。
- 第二个事件持有不同异常，且不影响第一个事件。
白盒断言（`testWhiteBox`）：public class、继承 `EventObject`、方法与构造器签名。
