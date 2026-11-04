# RMIConnection 测试设计

## 1. API 功能
`javax.management.remote.rmi.RMIConnection`（接口，继承 `java.io.Closeable` 与 `java.rmi.Remote`）是 RMI 连接器客户端/服务器之间的远程 `MBeanServerConnection` 接口。

- 方法：`getConnectionId()`、`close()`、4 个 `createMBean` 重载、`unregisterMBean`、`getObjectInstance`、`queryMBeans/queryNames`、`isRegistered`、`getMBeanCount`、`getAttribute/getAttributes`、`setAttribute/setAttributes`、`invoke`、`getDefaultDomain/getDomains`、`getMBeanInfo`、`isInstanceOf`、`addNotificationListener(s)`、`removeNotificationListener(s)`、`fetchNotifications`。

## 2. Java 用户如何使用
```java
RMIConnection conn = server.newClient(credentials);
conn.getConnectionId();
conn.getAttribute(name, "SomeAttr", subject);
NotificationResult nr = conn.fetchNotifications(cursor, 10, 0);
conn.close();
```
典型组合链路：`RMIServer.newClient → getAttribute/invoke/fetchNotifications → close`。

## 3. 开源/源码实际用法调研
- JDK 实现 `src/java.management.rmi/share/classes/javax/management/remote/rmi/RMIConnectionImpl.java`：`implements RMIConnection`，实现全部远程操作。
- JDK 生成 `RMIConnectionImpl_Stub`（`implements RMIConnection`）：客户端 stub。
来源均为 JDK 仓库（jdk25u-dev）源码。

## 4. 测试用例设计
白盒：`isInterface()`、继承 `Closeable` 与 `Remote`、关键方法存在（含 `fetchNotifications`）。
黑盒轻行为：用 `java.lang.reflect.Proxy` 动态实现（避免手写约 24 个方法），验证 `getConnectionId/fetchNotifications/close` 可调用并返回预期值。
