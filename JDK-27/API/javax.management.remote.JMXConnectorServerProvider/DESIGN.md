# JMXConnectorServerProvider 测试设计

## 1. API 功能
`javax.management.remote.JMXConnectorServerProvider`（接口）是连接器服务器提供者 SPI。

- 方法：`JMXConnectorServer newJMXConnectorServer(JMXServiceURL serviceURL, Map<String,?> environment, MBeanServer mbs) throws IOException`。

## 2. Java 用户如何使用
```java
JMXConnectorServerProvider provider = new JMXConnectorServerProvider() {
    public JMXConnectorServer newJMXConnectorServer(JMXServiceURL url, Map<String,?> env, MBeanServer mbs) {
        return new RMIConnectorServer(url, env, mbs);
    }
};
```
典型组合链路：`JMXConnectorServerFactory` 按协议定位并调用 `JMXConnectorServerProvider.newJMXConnectorServer` 创建服务器。

## 3. 开源/源码实际用法调研
- JDK 实现 `src/java.management/share/classes/javax/management/remote/JMXConnectorServerFactory.java`：通过 `ServiceLoader` 查找 `JMXConnectorServerProvider` 并调用。
- JDK rmi 提供者包内提供 `JMXConnectorServerProvider` 实现。
来源均为 JDK 仓库（jdk25u-dev）源码。

## 4. 测试用例设计
白盒：`isInterface()`、`newJMXConnectorServer` 签名与返回类型。
黑盒轻行为：匿名实现验证 `newJMXConnectorServer(url, null, null)` 可调用。
