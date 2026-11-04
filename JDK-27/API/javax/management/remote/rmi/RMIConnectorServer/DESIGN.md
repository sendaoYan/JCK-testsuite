# RMIConnectorServer 测试设计

## 1. API 功能
`javax.management.remote.rmi.RMIConnectorServer`（继承 `JMXConnectorServer`）是 RMI 连接器服务器实现。

- 常量：`JNDI_REBIND_ATTRIBUTE`、`RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE`、`RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE`、`CREDENTIALS_FILTER_PATTERN`、`SERIAL_FILTER_PATTERN`。
- 构造器：`(JMXServiceURL, Map)`、`(JMXServiceURL, Map, MBeanServer)`、`(JMXServiceURL, Map, RMIServerImpl, MBeanServer)`。
- 方法：`start/stop/isActive`、`getAddress()`（未启动返回 null）、`getAttributes()`、`getMBeanServer()`、`toJMXConnector`、`setMBeanServerForwarder`。

## 2. Java 用户如何使用
```java
RMIConnectorServer server = new RMIConnectorServer(url, null, mbs);
server.getMBeanServer();      // mbs
server.isActive();            // false（未 start）
server.start();               // 导出 RMI 并绑定
server.stop();
```
典型组合链路：`newJMXConnectorServer → start → 客户端连接 → stop`。

## 3. 开源/源码实际用法调研
- JDK 实现 `src/java.management.rmi/share/classes/javax/management/remote/rmi/RMIConnectorServer.java`：`getAddress()` 在 `!isActive()` 时返回 null；构造保存 `address`。
- `test/jdk` 多处经 `JMXConnectorServerFactory` 间接使用。
来源均为 JDK 仓库（jdk25u-dev）源码与 `test/jdk`。

## 4. 测试用例设计
白盒：继承 `JMXConnectorServer`、关键方法存在。
黑盒轻行为：五个常量精确值；构造后 `getMBeanServer` 保留、未激活、`getAddress` 为 null、`getConnectionIds` 为空、`getAttributes` 非 null 且空。
