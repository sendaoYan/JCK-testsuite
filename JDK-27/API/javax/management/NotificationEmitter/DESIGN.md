# javax.management.NotificationEmitter 黑盒行为测试设计

## 1. API 功能
- 扩展 NotificationBroadcaster，增加 `removeNotificationListener(NotificationListener, NotificationFilter, Object)` 两个重载。

## 2. Java 用户如何使用
需要按（listener,filter,handback）精确移除监听器的通知源实现此接口。
```java
emitter.removeNotificationListener(listener, filter, handback);
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/proxy/NotificationEmitterProxy.java`。
- `test/jdk/javax/management/notification/*Test.java`。
- 实现权威：`src/java.management/share/classes/javax/management/NotificationEmitter.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：用 NotificationBroadcasterSupport 实现验证精确 removeNotificationListener 只移除匹配 filter+handback 的监听器。
- 白盒：public 接口、父接口 NotificationBroadcaster、含两个精确 removeNotificationListener 重载。

## 深化（2026-11，批次 d）
- 声明面契约锁定：接口恰声明 1 个方法 removeNotificationListener(NotificationListener, NotificationFilter, Object)，声明恰一个受检异常 ListenerNotFoundException，返回 void。
- 多注册/移除闭环（经 NotificationBroadcasterSupport）：同 listener 双注册（不同 handback）每次 send 各投递一次；mismatched handback 三参 remove → ListenerNotFoundException；精确 (l,f,h) remove 只移除该注册、另一注册继续投递；再 remove 已删注册 → ListenerNotFoundException；继承单参 remove 一次清掉剩余注册；NotificationBroadcasterSupport is-a NotificationEmitter、NotificationEmitter is-a NotificationBroadcaster。

### 版本差异
- 无（双侧 javap 签名与行为一致）。
