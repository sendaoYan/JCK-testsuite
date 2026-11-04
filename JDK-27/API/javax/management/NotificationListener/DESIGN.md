# javax.management.NotificationListener 黑盒行为测试设计

## 1. API 功能
- 接口：`handleNotification(Notification, Object handback)`。

## 2. Java 用户如何使用
实现该接口接收通知；handback 是注册时透传的对象。
```java
public void handleNotification(Notification n, Object handback) { ... }
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/notification/*Test.java`：监听器回调。
- `test/jdk/javax/management/MBeanServer/MBeanServerNotificationTest.java`。
- 实现权威：`src/java.management/share/classes/javax/management/NotificationListener.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：注册匿名监听器到 NotificationBroadcasterSupport，验证 handleNotification 收到同一通知与 handback。
- 白盒：public 接口、extends EventListener、含 handleNotification。
