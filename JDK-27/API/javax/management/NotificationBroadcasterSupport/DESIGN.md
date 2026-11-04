# javax.management.NotificationBroadcasterSupport 黑盒行为测试设计

## 1. API 功能
- 实现 NotificationEmitter；构造可带 MBeanNotificationInfo 与 Executor。
- `sendNotification(Notification)`；protected `handleNotification(listener, notification, handback)`。

## 2. Java 用户如何使用
通知发送方的默认实现，MBean 继承它即可广播通知。
```java
class MyMBean extends NotificationBroadcasterSupport implements MyMBeanMBean {
    void fire() { sendNotification(new Notification("t", this, seq)); }
}
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/notification/BroadcasterSupportDeadlockTest.java`、`NotifExecutorTest.java`。
- 实现权威：`src/java.management/share/classes/javax/management/NotificationBroadcasterSupport.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：getNotificationInfo 返回构造入参；addNotificationListener 后 sendNotification 分发同一通知与 handback；带 NotificationFilter 的监听器只收到匹配类型；removeNotificationListener 后不再收到；handleNotification 为 protected。
- 白盒：public 类、implements NotificationEmitter、含 sendNotification、protected handleNotification。
