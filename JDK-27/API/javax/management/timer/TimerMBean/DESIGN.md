# javax.management.timer.TimerMBean — 设计案

## 1. API 功能
- 接口，定义 Timer 的管理接口。
- 方法含 start/stop、addNotification 三形态、removeNotification/removeNotifications/removeAllNotifications、getAllNotificationIDs/getNotificationIDs、逐通知查询、getDate/getPeriod/getNbOccurences/getFixedRate、get/setSendPastNotifications、isActive/isEmpty。

## 2. Java 用户如何使用
通过接口引用配置定时器。
```java
TimerMBean t = new Timer();
Integer id = t.addNotification("type", "msg", "ud", new Date(future));
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/management/timer/TimerMBeanTest.java` 通过接口覆盖添加/查询/移除。
- 说明：接口为契约，测试聚焦方法存在性与接口级状态读写。

## 4. 测试用例设计
- 白盒：接口、方法含 start/stop/addNotification/removeNotification/removeNotifications/removeAllNotifications/getNbNotifications/getAllNotificationIDs/getNotificationIDs/getNotificationType/getNotificationMessage/getNotificationUserData/getDate/getPeriod/getNbOccurences/getFixedRate/getSendPastNotifications/setSendPastNotifications/isActive/isEmpty。
- 黑盒：用 Timer 实例通过接口添加未来日期的通知、查询数量/ID/类型、移除后 isEmpty。
