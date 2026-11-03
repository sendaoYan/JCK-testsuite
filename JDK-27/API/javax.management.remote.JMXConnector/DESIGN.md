# JMXConnector 测试设计

## 1. API 功能
`javax.management.remote.JMXConnector`（接口，继承 `java.io.Closeable`）是到连接器服务器的客户端连接。

- 常量：`CREDENTIALS`（`jmx.remote.credentials`）。
- 方法：`connect()`、`connect(Map env)`、`getMBeanServerConnection()`、`getMBeanServerConnection(Subject)`（default 方法）、`close()`、`addConnectionNotificationListener(listener, filter, handback)`、`removeConnectionNotificationListener(...)`（两个重载）、`getConnectionId()`。

## 2. Java 用户如何使用
```java
JMXConnector c = JMXConnectorFactory.connect(url, env);
c.connect(env);
MBeanServerConnection mbsc = c.getMBeanServerConnection();
mbsc.queryNames(...);
c.addConnectionNotificationListener(l, null, null);
c.close();
```
典型组合链路：`connect → getMBeanServerConnection → 操作 MBean → close`；连接状态通过 `JMXConnectionNotification` 通知。

## 3. 开源/源码实际用法调研
- `test/jdk/com/sun/tools/attach/StartManagementAgent.java`（217–223 行）：`JMXConnector c = JMXConnectorFactory.connect(url, env)` 后操作连接。
- JDK 实现 `src/java.management.rmi/share/classes/javax/management/remote/rmi/RMIConnector.java`：`implements JMXConnector`，是标准实现。
来源均为 JDK 仓库（jdk25u-dev）源码与 `test/jdk`。

## 4. 测试用例设计
白盒：`isInterface()`、继承 `Closeable`、关键方法存在、`CREDENTIALS` 常量值。
黑盒轻行为：匿名实现验证 `connect/getConnectionId/getMBeanServerConnection/close` 可调用，`close` 后置位可观察。
