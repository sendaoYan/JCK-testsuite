# javax.management.Notification 黑盒行为测试设计

## 1. API 功能
- 继承 `java.util.EventObject`。
- 构造：`(type, source, sequenceNumber)`、`(type, source, sequenceNumber, timeStamp)`、`(type, source, sequenceNumber, timeStamp, message)`。
- 查询/设置：get/set Type、SequenceNumber、TimeStamp、Message、UserData、Source。

## 2. Java 用户如何使用
所有 JMX 通知的基类；通知发送方构造后交给 NotificationBroadcasterSupport 分发。
```java
Notification n = new Notification("jmx.attribute.change", src, seq, ts, "changed");
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/notification/NotifInfoTest.java`。
- `test/jdk/javax/management/monitor/*`。
- 实现权威：`src/java.management/share/classes/javax/management/Notification.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：5 参构造返回各字段；3/4 参构造 message 默认为空串 ""（源码确认）；setSequenceNumber/setTimeStamp/setUserData/setSource 生效；toString 含 type。
- 白盒：public 类、父类 EventObject、含 getType/getSequenceNumber/getTimeStamp/getMessage/getUserData 及对应 setter。
