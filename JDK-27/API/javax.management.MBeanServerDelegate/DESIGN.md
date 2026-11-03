# javax.management.MBeanServerDelegate 黑盒行为测试设计

## 1. API 功能
- 具体 delegate 类，实现 MBeanServerDelegateMBean 和 NotificationEmitter。
- 常量：spec 名 "Java Management Extensions"、spec 版本 "1.4"；`getMBeanServerId()` 唯一标识服务器。
- `sendNotification(Notification)` 发通知。

## 2. Java 用户如何使用
每个 MBeanServer 有一个 delegate，注册名 `JMImplementation:type=MBeanServerDelegate`；用户读 MBeanServerId 或订阅 delegate 通知。
```java
String id = (String) mbs.getAttribute(delegateName, "MBeanServerId");
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/ImplementationVersion/`：spec/impl 版本与厂商。
- `test/jdk/javax/management/MBeanServer/MBeanServerNotificationTest.java`：delegate 注册/注销通知。
- 实现权威：`src/java.management/share/classes/javax/management/MBeanServerDelegate.java`、`com/sun/jmx/defaults/ServiceName.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：getMBeanServerId 非空且不同服务器不同；getSpecificationName=="Java Management Extensions"、getSpecificationVersion=="1.4"、getSpecificationVendor/getImplementationName/getImplementationVendor 非空；getImplementationVersion 非空；MBeanServer 上读取 delegate 的 MBeanServerId 一致；发送通知被监听器收到。
- 白盒：public 类、implements MBeanServerDelegateMBean、implements NotificationEmitter。
