# JMXConnectorFactory 测试设计

## 1. API 功能
`javax.management.remote.JMXConnectorFactory`（工具类，私有构造）创建 JMX 连接器客户端。

- 常量：`PROTOCOL_PROVIDER_PACKAGES`、`DEFAULT_CLASS_LOADER`、`PROTOCOL_PROVIDER_CLASS_LOADER`。
- 静态方法：`connect(JMXServiceURL)`、`connect(JMXServiceURL, Map)`、`newJMXConnector(JMXServiceURL, Map)`（仅创建实例，不建立连接）。

## 2. Java 用户如何使用
```java
JMXConnector c = JMXConnectorFactory.newJMXConnector(url, null); // 不连接
// 或
JMXConnector c2 = JMXConnectorFactory.connect(url, env);         // 创建并连接
```
典型组合链路：`newJMXConnector` 用协议名查提供者包，实例化 `RMIConnector`；`connect` 再调用 `connect(env)`。

## 3. 开源/源码实际用法调研
- `test/jdk/com/sun/tools/attach/StartManagementAgent.java`（217–223 行）：`JMXConnectorFactory.connect(url, env)`。
- `test/jdk/com/sun/jmx/remote/NotificationMarshalVersions/Client/Client.java`：`JMXConnectorFactory.connect`。
- JDK 实现 `src/java.management/share/classes/javax/management/remote/JMXConnectorFactory.java`：协议名解析 + 服务加载逻辑。
来源均为 JDK 仓库（jdk25u-dev）源码与 `test/jdk`。

## 4. 测试用例设计
白盒：无公共构造器、`connect/newJMXConnector` 方法存在。
黑盒轻行为：三个常量精确值；`newJMXConnector(rmi url, null)` 返回 `RMIConnector`（未连接）；未连接时 `getConnectionId()` 抛 `IOException("Not connected")`；未知协议抛 `MalformedURLException("Unsupported protocol: ...")`。
