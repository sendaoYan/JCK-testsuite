# JMXConnectorServerFactory 测试设计

## 1. API 功能
`javax.management.remote.JMXConnectorServerFactory`（工具类，私有构造）创建连接器服务器。

- 常量：`PROTOCOL_PROVIDER_PACKAGES`、`DEFAULT_CLASS_LOADER`、`DEFAULT_CLASS_LOADER_NAME`、`PROTOCOL_PROVIDER_CLASS_LOADER`。
- 静态方法：`newJMXConnectorServer(JMXServiceURL, Map, MBeanServer) throws IOException`。

## 2. Java 用户如何使用
```java
JMXConnectorServer server = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbs);
// server 尚未启动（isActive() == false）
server.start();
```
典型组合链路：按协议查找 `JMXConnectorServerProvider`，创建未启动的服务器实例，随后 `start/stop`。

## 3. 开源/源码实际用法调研
- `test/jdk/com/sun/jmx/remote/NotificationMarshalVersions/Server/Server.java`、`test/jdk/com/sun/management/DiagnosticCommandMBean/DcmdMBeanTest.java`：`JMXConnectorServerFactory.newJMXConnectorServer(...)` 后 `start()`。
- JDK 实现 `src/java.management/share/classes/javax/management/remote/JMXConnectorServerFactory.java`：服务加载逻辑。
来源均为 JDK 仓库（jdk25u-dev）源码与 `test/jdk`。

## 4. 测试用例设计
白盒：无公共构造器、`newJMXConnectorServer` 存在。
黑盒轻行为：四个常量精确值；`newJMXConnectorServer(rmi url, null, mbs)` 返回 `RMIConnectorServer`、未激活、`getMBeanServer()` 保留、`getAddress()` 为 null、`getConnectionIds()` 为空。
