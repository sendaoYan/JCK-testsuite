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
