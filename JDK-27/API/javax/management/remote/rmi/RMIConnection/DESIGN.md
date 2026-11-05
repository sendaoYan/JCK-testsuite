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

## 5. 深化（2026-11）
白盒新增：27 方法名+参数计数全集矩阵锁定（createMBean 3/4/5/6 四形态、invoke 5 参、setAttribute 3 参等）；全部方法声明 IOException 且 abstract；`fetchNotifications` 返回 `NotificationResult`。
黑盒新增（真协议回环，RMIJRMPServerImpl+RMIConnectorServer，127.0.0.1 动态端口）：`newClient(null)` 连接 id 以 `rmi://127.0.0.1 ` 开头；`getAttribute`/`setAttribute(MarshalledObject)`+回读/`invoke add(2)`/`getAttributes`（元素显式 cast）/`getObjectInstance`/远程 `createMBean`/`getMBeanCount`/`unregisterMBean`/`queryNames`/`isInstanceOf`/`getDefaultDomain`/`fetchNotifications(0,10,0)` 立即返回；`rc.close()` 后死 stub 抛 IOException。
版本差异：与 JDK-8 侧方法集完全一致（27 个）；连接 id 无 Subject 时双空格 quirk 双侧相同；本版本 Subject delegation 已移除（见 RMIConnectionImpl）。
