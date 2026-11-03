# JMXConnectorServerMBean 测试设计

## 1. API 功能
`javax.management.remote.JMXConnectorServerMBean`（接口）是连接器服务器的 MBean 管理接口。

- 方法：`start()`、`stop()`、`isActive()`、`setMBeanServerForwarder(MBeanServerForwarder)`、`getConnectionIds()`、`getAddress()`、`getAttributes()`、`toJMXConnector(Map)`。

## 2. Java 用户如何使用
```java
JMXConnectorServerMBean mbean = ...; // 如 RMIConnectorServer
mbean.start();
mbean.isActive();
mbean.getConnectionIds();
mbean.stop();
```
典型组合链路：管理端通过该接口启停连接器服务器、查询连接 ID、读取地址与属性。

## 3. 开源/源码实际用法调研
- JDK 实现 `src/java.management/share/classes/javax/management/remote/JMXConnectorServer.java`：`implements JMXConnectorServerMBean`。
- JDK 实现 `src/java.management.rmi/share/classes/javax/management/remote/rmi/RMIConnectorServer.java`：继承并实现该接口方法。
来源均为 JDK 仓库（jdk25u-dev）源码。

## 4. 测试用例设计
白盒：`isInterface()`、8 个方法存在。
黑盒轻行为：匿名实现验证 `start/stop/isActive` 状态翻转、`getConnectionIds` 返回数组、`getAttributes` 返回空 map。
