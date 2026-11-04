# JMXAddressable 测试设计

## 1. API 功能
`javax.management.remote.JMXAddressable`（接口）提供连接器/连接器服务器的地址。

- 方法：`JMXServiceURL getAddress()` —— 返回地址，可为 null（如仅持有 RMI stub 的连接器）。

## 2. Java 用户如何使用
```java
JMXAddressable a = new JMXConnectorServer(...) /* 或 JMXConnector 实现 */;
a.getAddress(); // 服务/连接地址
```
典型组合链路：`RMIConnector`、`JMXConnectorServer` 均实现该接口；用户对任一连接端点统一调用 `getAddress()`。

## 3. 开源/源码实际用法调研
- JDK 实现 `src/java.management.rmi/share/classes/javax/management/remote/rmi/RMIConnector.java`（`getAddress()` 返回 `jmxServiceURL`）与 `RMIConnectorServer.java`（未启动时返回 null）：JDK 内部实现点。
- JDK 实现 `src/java.management/share/classes/javax/management/remote/JMXConnectorServer.java`：声明实现 `JMXAddressable`。
来源均为 JDK 仓库（jdk25u-dev）源码。

## 4. 测试用例设计
白盒：`isInterface()`、`getAddress()` 返回类型为 `JMXServiceURL`。
黑盒轻行为：匿名实现证明 `getAddress()` 可调用，且能返回 null 或指定值。
