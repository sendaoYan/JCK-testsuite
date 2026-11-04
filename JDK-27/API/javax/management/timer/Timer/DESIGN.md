# javax.management.timer.Timer — 设计案

## 1. API 功能
- 具体类，继承 `NotificationBroadcasterSupport` 并实现 `TimerMBean`、`MBeanRegistration`。
- 核心：`addNotification` 三个重载（一次性/周期/固定速率）；`removeNotification`/`removeNotifications`/`removeAllNotifications`；`getAllNotificationIDs`/`getNotificationIDs(type)`；逐通知查询 type/message/userData/date/period/nbOccurences/fixedRate；`isActive`/`isEmpty`；`setSendPastNotifications`。
- 常量：`ONE_SECOND`/`ONE_MINUTE`/`ONE_HOUR`/`ONE_DAY`/`ONE_WEEK`。

## 2. Java 用户如何使用
注册定时器后添加通知，指定触发时间与周期。
```java
Timer t = new Timer();
Integer id = t.addNotification("type", "msg", "user", new Date(futureMillis), 1000L, 5L);
t.start();
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/management/timer/TimerTest.java` 覆盖添加/查询/移除与校验。
- 说明：`addNotification` 会把过去的时间钳制为 now，故测试用未来固定毫秒值（4102444800000L）保证确定性；默认 `sendPastNotifications=false`。

## 4. 测试用例设计
- 白盒：非接口、继承 NotificationBroadcasterSupport、实现 TimerMBean/MBeanRegistration。
- 黑盒：初始空/inactive/零通知/默认不补发；多类型通知添加后 ID 唯一；按类型与全部检索；逐通知查询（周期/次数/固定速率）；removeNotification/removeNotifications/removeAllNotifications 及二次移除抛 InstanceNotFoundException；setSendPastNotifications 往返；null 日期/负周期抛 IllegalArgumentException；常量校验。
