# JMXConnectorServer：连接器服务器抽象基类（NotificationBroadcasterSupport + JMXConnectorServerMBean + MBeanRegistration + JMXAddressable） 测试设计（JDK-8 侧）

> 2026-11 批 m 契约深化：原有白盒反射冒烟之上追加 `testDeepened()`（黑盒契约 + RMI 真回环）。

## 1. API 功能
- 生命周期：start/stop 幂等；未 start 前 getAddress()=null、isActive=false；stop 后 getAddress()=null 且重启抛 `IOException: The server has been stopped.`；未 start 先 stop 为静默 no-op（之后同样不可再 start）；未挂 MBeanServer start → `IllegalStateException: This connector server is not attached to an MBean server`。
- 连接账目：getConnectionIds() 反映在线客户端（close 即移除）；getNotificationInfo() 通告 opened/closed/failed 三型（不含 notifs.lost）。
- 通知：OPENED（消息 `Connection opened`）/CLOSED（消息 `Client connection closed`），source 为服务器实例，seq 单调递增。
- toJMXConnector(null)：活动时返回可连接的客户端连接器；未活动 `IllegalStateException: Connector is not active`。
- setMBeanServerForwarder(null) → `IllegalArgumentException: Invalid null argument: mbsf`；保护构造器 JMXConnectorServer(MBeanServer) 经匿名子类实证 getMBeanServer 接线。

## 2. 测试用例设计与易错规范细节
- 生命周期矩阵、通知真闭环（服务器侧收 OPENED/CLOSED 且 connectionId 与客户端一致）、toJMXConnector 可连、负例四件（重启/提前 stop/未挂载/null forwarder）、匿名子类保护构造器。

## 3. 参考来源
- JDK8u-dev `RMIServerImpl.java:258`（"Connection opened"）与 `:365`（"Client connection closed"）；`JMXConnectorServer.java`；`RMIConnectorServer.java`；双侧运行实测。


### RMI 回环骨架（本目录批 m 实际采用）
`new JMXServiceURL("service:jmx:rmi://127.0.0.1")`（无端口、无 URL 路径）→ `JMXConnectorServerFactory.newJMXConnectorServer(url, env, server)` → `cs.start()` 后 `cs.getAddress()` 返回 `service:jmx:rmi://127.0.0.1/stub/<base64 RMIServer stub>`（stub 内嵌 URL，两侧行为一致）→ 客户端 `JMXConnectorFactory.connect(cs.getAddress(), env)`。全程 127.0.0.1 回环、零固定端口、无 LocateRegistry、无端口冲突；finally 中 `conn.close()` + `cs.stop()` 释放（不 stop 会因 RMI 非守护线程挂起 JVM）。备用 JNDI 形态 `service:jmx:rmi:///jndi/rmi://127.0.0.1:<动态端口>/jmxr`（LocateRegistry.createRegistry + ServerSocket(0) 探测）经双侧实测亦可用，但 stub 形态更干净，未采用。

### 全局版本差异（JDK-8 vs JDK-27，实测锁定）
1. Subject Delegation 移除：JDK-27 `JMXConnector.getMBeanServerConnection(Subject)` 抛 `UnsupportedOperationException: Subject Delegation has been removed.`（JDK-8 正常发放委托连接；未认证连接上首个远程操作抛 `SecurityException: Subject delegation cannot be enabled unless an authenticated subject is put in place`）；`SubjectDelegationPermission` 在 JDK-27 标记 `@Deprecated(forRemoval=true)` 但构造与 implies 语义不变。
2. `new JMXServiceURL((String)null)` 的 NPE 消息：JDK-8 无消息，JDK-27 为 `Cannot invoke "String.length()" because "serviceURL" is null`（测试两侧均只断言 NPE 类型）。
3. 其余异常类型与精确消息（见各目录负例表）双侧一致。
