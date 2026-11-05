# JMXConnector：客户端连接器接口（Closeable） 测试设计（JDK-8 侧）

> 2026-11 批 m 契约深化：原有白盒反射冒烟之上追加 `testDeepened()`（黑盒契约 + RMI 真回环）。

## 1. API 功能
- getConnectionId 前缀 `rmi://127.0.0.1`（认证后中段插入 principal 名）；同一连接两次 getMBeanServerConnection() 返回同一实例；connect(Map) 二次调用幂等；close 幂等。
- close 后：操作 → `IOException: The client has been closed.`；getConnectionId → `IOException: Not connected`；getMBeanServerConnection → IOException（消息 "Connection closed"，只断言类型）；未 connect 的连接器 getConnectionId → `IOException: Not connected`。
- 客户端侧连接通知：close → 收 CLOSED（type/connectionId/source=连接器本体）；removeConnectionNotificationListener 未注册 → `ListenerNotFoundException: Listener not registered`。
- 远程操作全矩阵与本地同值（getAttribute/setAttribute/invoke/queryNames/getMBeanInfo/getDefaultDomain/getMBeanCount/createMBean/unregisterMBean/InstanceNotFoundException）。
- Subject 委托：版本差异见下。

## 2. 测试用例设计与易错规范细节
- 连接生命周期 + 远程全操作闭环 + 客户端通知闭环 + close 后异常精确消息 + 未连接态。

## 3. 参考来源
- JDK8u-dev `RMIConnector.java`（"Not connected"/"Connection closed"/"The client has been closed."）；Oracle javadoc JMXConnector；双侧运行实测。


### RMI 回环骨架（本目录批 m 实际采用）
`new JMXServiceURL("service:jmx:rmi://127.0.0.1")`（无端口、无 URL 路径）→ `JMXConnectorServerFactory.newJMXConnectorServer(url, env, server)` → `cs.start()` 后 `cs.getAddress()` 返回 `service:jmx:rmi://127.0.0.1/stub/<base64 RMIServer stub>`（stub 内嵌 URL，两侧行为一致）→ 客户端 `JMXConnectorFactory.connect(cs.getAddress(), env)`。全程 127.0.0.1 回环、零固定端口、无 LocateRegistry、无端口冲突；finally 中 `conn.close()` + `cs.stop()` 释放（不 stop 会因 RMI 非守护线程挂起 JVM）。备用 JNDI 形态 `service:jmx:rmi:///jndi/rmi://127.0.0.1:<动态端口>/jmxr`（LocateRegistry.createRegistry + ServerSocket(0) 探测）经双侧实测亦可用，但 stub 形态更干净，未采用。

### 全局版本差异（JDK-8 vs JDK-27，实测锁定）
1. Subject Delegation 移除：JDK-27 `JMXConnector.getMBeanServerConnection(Subject)` 抛 `UnsupportedOperationException: Subject Delegation has been removed.`（JDK-8 正常发放委托连接；未认证连接上首个远程操作抛 `SecurityException: Subject delegation cannot be enabled unless an authenticated subject is put in place`）；`SubjectDelegationPermission` 在 JDK-27 标记 `@Deprecated(forRemoval=true)` 但构造与 implies 语义不变。
2. `new JMXServiceURL((String)null)` 的 NPE 消息：JDK-8 无消息，JDK-27 为 `Cannot invoke "String.length()" because "serviceURL" is null`（测试两侧均只断言 NPE 类型）。
3. 其余异常类型与精确消息（见各目录负例表）双侧一致。
