# JMXConnectorProvider 测试设计

## 1. API 功能
`javax.management.remote.JMXConnectorProvider`（接口）是连接器客户端提供者 SPI。

- 方法：`JMXConnector newJMXConnector(JMXServiceURL serviceURL, Map<String,?> environment) throws IOException`。

## 2. Java 用户如何使用
```java
JMXConnectorProvider provider = new JMXConnectorProvider() {
    public JMXConnector newJMXConnector(JMXServiceURL url, Map<String,?> env) {
        return new RMIConnector(url, env);
    }
};
```
典型组合链路：`JMXConnectorFactory` 按 `serviceURL.getProtocol()` 定位提供者包，反射加载 `JMXConnectorProvider` 实现并调用 `newJMXConnector`。

## 3. 开源/源码实际用法调研
- JDK 实现 `src/java.management.rmi/share/classes/javax/management/remote/rmi/RMIConnectorServer.java` 附近：`com.sun.jmx.remote.internal` 与 rmi 提供者包内实现 `JMXConnectorProvider`。
- JDK 实现 `src/java.management/share/classes/javax/management/remote/JMXConnectorFactory.java`：通过 `ServiceLoader` 查找 `JMXConnectorProvider`。
来源均为 JDK 仓库（jdk25u-dev）源码。

## 4. 测试用例设计
白盒：`isInterface()`、`newJMXConnector` 签名与返回类型。
黑盒轻行为：匿名实现验证 `newJMXConnector(url, null)` 可调用。
