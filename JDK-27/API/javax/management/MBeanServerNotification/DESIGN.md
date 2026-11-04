# javax.management.MBeanServerNotification 黑盒行为测试设计

## 1. API 功能
- 常量：`REGISTRATION_NOTIFICATION`（"JMX.mbean.registered"）、`UNREGISTRATION_NOTIFICATION`（"JMX.mbean.unregistered"）。
- 构造：`(String type, Object source, long sequenceNumber, ObjectName objectName)`。
- 查询：`getMBeanName()`。

## 2. Java 用户如何使用
MBeanServer 注册/注销 MBean 时发出该通知；监听 delegate 的客户端据此维护本地缓存。
```java
public void handleNotification(Notification n, Object hb) {
    if (n instanceof MBeanServerNotification) {
        ObjectName on = ((MBeanServerNotification) n).getMBeanName();
    }
}
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/MBeanServer/MBeanServerNotificationTest.java`、`NewMBeanListenerTest.java`。
- 实现权威：`src/java.management/share/classes/javax/management/MBeanServerNotification.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：构造后 getType 返回入参、getMBeanName 返回 ObjectName；常量值正确；继承 Notification 的 getSource/getSequenceNumber。
- 白盒：public 类、父类 Notification、含 getMBeanName。
