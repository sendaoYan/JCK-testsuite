# javax.management.timer.TimerNotification — 设计案

## 1. API 功能
- 具体类，继承 `Notification`。
- 唯一扩展查询：`getNotificationID()` 返回触发该通知的定时通知 ID（Integer）。
- 单 public 构造器：`(String type, Object source, long sequenceNumber, long timeStamp, String message, Integer notificationID)`。

## 2. Java 用户如何使用
作为 Timer 派发通知的对象类型，读取对应的通知 ID。
```java
if (n instanceof TimerNotification tn) {
    Integer id = tn.getNotificationID();
}
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/management/timer/TimerNotificationTest.java` 覆盖构造器与 getNotificationID。
- 说明：其余行为继承 Notification，测试聚焦 ID 传递与继承字段往返、userData 设置、null ID。

## 4. 测试用例设计
- 白盒：继承 Notification、单 public 构造器。
- 黑盒：构造后 type/sequenceNumber/timeStamp/message/source 继承字段正确；getNotificationID 往返；null ID 保留；setUserData/getUserData 继承可用。
