# RMIConnector 测试设计

## 1. API 功能
`javax.management.remote.rmi.RMIConnector`（实现 `JMXConnector`、`Serializable`、`JMXAddressable`）是 RMI 连接器客户端实现。

- 构造器：`(JMXServiceURL url, Map env)`、`(RMIServer rmiServer, Map env)`；二者同为 null 抛 `IllegalArgumentException`。
- 方法：`getAddress()`（URL 构造有值、stub 构造为 null）、`toString()`、`connect()/connect(env)`、`getMBeanServerConnection()`、`getConnectionId()`（未连接抛 `IOException("Not connected")`）、`close()`、通知监听器方法。

## 2. Java 用户如何使用
```java
RMIConnector rc = new RMIConnector(url, null);
rc.getAddress();         // url
rc.connect(env);
rc.getMBeanServerConnection();
rc.close();
```
典型组合链路：`JMXConnectorFactory.newJMXConnector(url, env)` 实际返回 `RMIConnector`；`connect → 操作 → close`。

## 3. 开源/源码实际用法调研
- JDK 实现 `src/java.management.rmi/share/classes/javax/management/remote/rmi/RMIConnector.java`：私有构造校验 `rmiServer and jmxServiceURL both null`，`getAddress()` 返回 `jmxServiceURL`。
- `test/jdk` 多处经 `JMXConnectorFactory` 间接使用 `RMIConnector`。
来源均为 JDK 仓库（jdk25u-dev）源码与 `test/jdk`。

## 4. 测试用例设计
白盒：实现 `JMXConnector`/`Serializable`/`JMXAddressable`、关键方法存在。
黑盒轻行为：URL 构造保留 `getAddress`、`toString` 含 URL、未连接 `getConnectionId` 抛 `IOException("Not connected")`、`close()` 安全；stub 构造 `getAddress` 为 null；双 null 抛 `IllegalArgumentException`。
