# javax.management.NotificationBroadcaster 黑盒行为测试设计

## 1. API 功能
- 接口：`addNotificationListener(NotificationListener, NotificationFilter, Object)`、`removeNotificationListener(NotificationListener)`、`getNotificationInfo()`。

## 2. Java 用户如何使用
通知源实现此接口；客户端注册监听器接收通知。
```java
broadcaster.addNotificationListener(listener, filter, handback);
broadcaster.removeNotificationListener(listener);
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/notification/*Test.java`：监听器注册/过滤/移除。
- 实现权威：`src/java.management/share/classes/javax/management/NotificationBroadcaster.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：用 NotificationBroadcasterSupport 作实现验证 addNotificationListener/getNotificationInfo/removeNotificationListener 契约。
- 白盒：public 接口、含 addNotificationListener/removeNotificationListener/getNotificationInfo。
