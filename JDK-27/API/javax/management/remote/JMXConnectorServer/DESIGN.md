# JMXConnectorServer 测试设计

## 1. API 功能
`javax.management.remote.JMXConnectorServer`（抽象类，继承 `NotificationBroadcasterSupport`，实现 `JMXConnectorServerMBean`、`MBeanRegistration`、`JMXAddressable`）是连接器服务器基类。

- 常量：`AUTHENTICATOR`（`jmx.remote.authenticator`）。
- 构造器：`()`、`(MBeanServer mbs)`。
- 方法：`getMBeanServer()`、`setMBeanServerForwarder(MBeanServerForwarder)`、`getConnectionIds()`、`toJMXConnector(Map)`、`getNotificationInfo()`、`connectionOpened/Closed/Failed`（受保护）、`preRegister/postRegister/preDeregister/postDeregister`。
- 抽象（来自接口）：`start/stop/isActive/getAddress/getAttributes`。

## 2. Java 用户如何使用
```java
JMXConnectorServer server = new RMIConnectorServer(url, null, mbs);
server.start();
server.isActive();
server.getNotificationInfo();
server.stop();
```
典型组合链路：`newJMXConnectorServer → start → 客户端连接 → 通知 → stop`。

## 3. 开源/源码实际用法调研
- JDK 实现 `src/java.management/share/classes/javax/management/remote/JMXConnectorServer.java`：`getNotificationInfo()` 返回包含 OPENED/CLOSED/FAILED 三种类型的 `MBeanNotificationInfo`。
- JDK 实现 `src/java.management.rmi/share/classes/javax/management/remote/rmi/RMIConnectorServer.java`：`extends JMXConnectorServer`。
来源均为 JDK 仓库（jdk25u-dev）源码。

## 4. 测试用例设计
白盒：抽象类、继承/实现关系、关键方法存在、`AUTHENTICATOR` 常量值。
黑盒轻行为：匿名子类实现 5 个抽象方法，验证 `getMBeanServer` 保留、`getConnectionIds` 初始为空、`getNotificationInfo` 三个类型精确、`start/stop/isActive` 状态翻转。
