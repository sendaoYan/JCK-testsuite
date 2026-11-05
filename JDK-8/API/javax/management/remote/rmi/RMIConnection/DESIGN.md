# RMIConnection 测试设计

## 1. API 功能
`javax.management.remote.rmi.RMIConnection` 是 RMI 连接器客户端与服务器之间的连接级远程接口（每个已连接客户端一个）。

- 恰好 27 个方法（名称+参数计数全集锁定）：`getConnectionId`、`close`、`createMBean`×4 形态（3/4/5/6 参）、`getAttribute`、`getAttributes`、`setAttribute`、`setAttributes`、`invoke`（5 参）、`getDefaultDomain`、`getDomains`、`getMBeanCount`、`isRegistered`、`isInstanceOf`、`getObjectInstance`、`queryMBeans`、`queryNames`、`registerMBean`、`unregisterMBean`、`addNotificationListener`（对象参 5 参）、`addNotificationListeners`、`removeNotificationListener`×2 形态、`removeNotificationListeners`、`fetchNotifications`。
- 全部方法声明 `throws IOException`（接口为 abstract）；`fetchNotifications` 返回 `NotificationResult`。
- 复杂参数以 `MarshalledObject` 封装（`getAttribute`/`invoke` 的参数与 `setAttribute` 的值），脱敏过滤由 Subject delegation 控制。

## 2. Java 用户如何使用
```java
JMXConnector jmxc = JMXConnectorFactory.connect(url, env);
MBeanServerConnection mbsc = jmxc.getMBeanServerConnection(); // 底层即 RMIConnection
mbsc.getAttribute(name, "Count");                             // 路由到 getAttribute
```
用户不直接实现/调用该接口；`RMIConnector` 在客户端内部为其生成动态代理并经 `RMIConnectionImpl_Stub` 编组。

## 3. 开源/源码实际用法调研
- JDK 源 `RMIConnection.java`（jdk8u-dev）：接口契约即上述方法集；`RMIConnectionImpl` 为服务端实现、`RMIConnectionImpl_Stub` 为 rmic 生成的客户端 stub。
- `test/jdk/javax/management` 经 `JMXConnector` 间接驱动本接口；协议级直连（`RMIServer.newClient`）见远程/通知测试。
来源：JDK 仓库源码 javap 双侧（JDK-8/JDK-27）对照。

## 4. 测试用例设计
白盒：反射锁定 27 方法名+参数计数矩阵、全部声明 IOException+abstract、`invoke`/`setAttribute`/`fetchNotifications` 精确签名。
黑盒深化：
- 录制型 Proxy：`getConnectionId`/`fetchNotifications` 恒等返回、`isRegistered`→false、`getMBeanCount`→null（接口不强制非 null 语义，仅验证代理可路由）。
- 真协议回环（`RMIJRMPServerImpl`+`RMIConnectorServer` stub-encoded URL）：`newClient(null)`→连接 id 以 `rmi://127.0.0.1 ` 开头、`getAttribute` 真值、`setAttribute(MarshalledObject(Attribute))`+回读、`invoke add(2)`、`getAttributes`（`((Attribute)list.get(0)).getValue()`）、`getObjectInstance`、远程 `createMBean` 注册、`getMBeanCount`、`unregisterMBean`、`queryNames`、`isInstanceOf`、`getDefaultDomain`、`fetchNotifications(0,10,0)` 立即返回不阻塞。
- 负例：`rc.close()` 后死 stub `getConnectionId`/`getAttribute` → IOException。

## 5. 深化（2026-11）与版本差异
- 双侧（JDK-8/JDK-27）断言集一致；27 方法矩阵两版本相同。
- 连接 id 格式 quirk：无 Subject 时为 `"rmi://<host> <空> <counter>"`——两空格，断言用 `startsWith("rmi://127.0.0.1 ")`。
- `getAttributes`/`queryMBeans` 等 List 返回需按元素显式 cast（Java 8 无泛型协变推断坑）。
- JDK-27 侧同类文件：`JDK-27/API/javax/management/remote/rmi/RMIConnection/`。
