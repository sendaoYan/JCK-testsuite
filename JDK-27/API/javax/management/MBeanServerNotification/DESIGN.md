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

## 深化（2026-11）：构造契约 + 真注册/注销通知闭环

### 新增覆盖
1. **构造 getter**：常量串全等（"JMX.mbean.registered"/"JMX.mbean.unregistered"）；type/mbeanName/seq/source 精确回读；toString 含 type 与 mbean 名；setUserData round-trip；**getMessage() 为空串 "" 而非 null（实测）**；null ObjectName 构造容忍（getMBeanName()==null）。
2. **真闭环**：监听本地 server 的 delegate → register+unregister 一个 MBean → 有界轮询（≤5s/50ms）恰收 2 条：先 REGISTRATION 后 UNREGISTRATION、getMBeanName 精确、**getSource()==DELEGATE_NAME**、序列号严格递增、handback 同一实例两次。
3. **过滤器分支**：只放行 UNREGISTRATION 的 filter → 注册被抑制、恰收 1 条注销通知。

### 参考来源
- Oracle javadoc: MBeanServerNotification（source 语义）。
- OpenJDK `test/jdk/javax/management/MBeanServer/MBeanServerNotificationTest.java`、`NewMBeanListenerTest.java`（思路）。
