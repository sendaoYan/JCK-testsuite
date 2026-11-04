# javax.naming.event.NamingEvent — 测试设计

## 1. API 功能
- 事件类型常量：`OBJECT_ADDED=0`、`OBJECT_REMOVED=1`、`OBJECT_RENAMED=2`、`OBJECT_CHANGED=3`。
- 构造器：`NamingEvent(EventContext source, int type, Binding newBd, Binding oldBd, Object changeInfo)`，继承 `java.util.EventObject`。
- 查询：`getType()`、`getEventContext()`、`getOldBinding()`、`getNewBinding()`、`getChangeInfo()`。
- 派发：`dispatch(NamingListener)` 按 `type` 调用监听器对应回调。

## 2. Java 用户如何使用
- 服务实现构造事件并派发；用户端在监听器里读取绑定与变更信息。
- 典型构造（JDK 源码）：`new NamingEvent(eventSrc, NamingEvent.OBJECT_ADDED, newBd, null, changeID)`。
- 组合链路：构造 `Binding`（old/new）→ `new NamingEvent(src, type, newBd, oldBd, changeInfo)` → `evt.dispatch(listener)` → `getOldBinding()/getNewBinding()/getChangeInfo()`。

## 3. 开源代码实际用法调研
来源：`~/git/jdk25u-dev`。
- `com/sun/jndi/ldap/NamingEventNotifier.java`：
  - add：`new NamingEvent(eventSrc, OBJECT_ADDED, newBd, null, changeID)`
  - remove：`new NamingEvent(eventSrc, OBJECT_REMOVED, null, oldBd, changeID)`
  - changed：`new NamingEvent(eventSrc, OBJECT_CHANGED, newBd, oldBd, changeID)`
  - renamed：`new NamingEvent(eventSrc, OBJECT_RENAMED, newBd, oldBd, changeID)`
- `javax/naming/event/NamingEvent.java` 的 `dispatch` 实现各类型分派。

## 4. 测试用例设计
黑盒断言（`NamingEventTest.testBlackBox`）：
- 四个类型常量精确值 0/1/2/3。
- 构造后 `getType/getEventContext/getSource/getOldBinding/getNewBinding/getChangeInfo` 身份一致，绑定名/对象值可读。
- `dispatch` 对四种类型分别路由到 `objectAdded/objectRemoved/objectRenamed/objectChanged`，且收到同一事件。
白盒断言（`testWhiteBox`）：public class、继承 `EventObject`、方法与公开构造器签名。
