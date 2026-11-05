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

## 深化（2026-11 批 I：monitor/timer 真闭环）

### 新增覆盖维度
- **构造+getter 全字段**：六参构造后 type/source/sequenceNumber/timeStamp/message 逐项回读 + getNotificationID 往返；null notificationID 合法保留。
- **继承可变量**：setUserData 覆写构造时 userData、setSequenceNumber 改序号（继承自 Notification 的公开行为）。
- **序列化**：svuid 锁定 `1798492029603825750`；round-trip 后全部六字段 + userData 保留。
- **真实投递集成**：Timer 真发出的通知 instanceof TimerNotification，getNotificationID 与 addNotification 返回 id 同一 Integer，type/message/userData 与注册值一致（端到端）。
- **TimerAlarmClockNotification 白盒**：存在性（非公开类、包私有构造、继承 Notification）、Timer 发出的运行时通知对象类型确认。

### 实测锁定（JDK-8 与 JDK-27 完全一致）
- Timer 类本身不可序列化（NotificationBroadcasterSupport 无 Serializable）；TimerNotification 可独立序列化，svuid 双侧相同。
