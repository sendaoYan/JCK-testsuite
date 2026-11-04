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

## 深化（2026-11）：作为 MBean 的 delegate 全属性闭环

### 新增覆盖
1. **本地 server 上的 delegate**（DELEGATE_NAME 预注册）：7 属性非空/常量精确（SpecificationName=="Java Management Extensions"、SpecificationVersion=="1.4" 双侧一致、ImplementationName=="JMX"；ImplementationVersion 为 JVM 版本串仅断非空）；MBeanInfo 属性名集合恰为 7 常量、**全部只读**（逐个断言 isReadable&&!isWritable）、0 操作、1 通知 info。
2. **写拒绝**：delegate 只读属性 setAttribute → `AttributeNotFoundException`（消息 "X not accessible"）。
3. **直接实例**：两个新 delegate 的 MBeanServerId 互异；delegate 可作为普通 MBean 注册到自定义名下且属性路由到同一实例。

### 参考来源
- Oracle javadoc: MBeanServerDelegate。
- OpenJDK `test/jdk/javax/management/ImplementationVersion/`（版本属性暴露思路）。
