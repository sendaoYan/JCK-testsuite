# JMXConnector 测试设计

## 1. API 功能
`javax.management.remote.JMXConnector`（接口，继承 `java.io.Closeable`）是到连接器服务器的客户端连接。

- 常量：`CREDENTIALS`（`jmx.remote.credentials`）。
- 方法：`connect()`、`connect(Map env)`、`getMBeanServerConnection()`、`getMBeanServerConnection(Subject)`（default 方法）、`close()`、`addConnectionNotificationListener(listener, filter, handback)`、`removeConnectionNotificationListener(...)`（两个重载）、`getConnectionId()`。

## 2. Java 用户如何使用
```java
JMXConnector c = JMXConnectorFactory.connect(url, env);
c.connect(env);
MBeanServerConnection mbsc = c.getMBeanServerConnection();
mbsc.queryNames(...);
c.addConnectionNotificationListener(l, null, null);
c.close();
```
典型组合链路：`connect → getMBeanServerConnection → 操作 MBean → close`；连接状态通过 `JMXConnectionNotification` 通知。

## 3. 开源/源码实际用法调研
- `test/jdk/com/sun/tools/attach/StartManagementAgent.java`（217–223 行）：`JMXConnector c = JMXConnectorFactory.connect(url, env)` 后操作连接。
- JDK 实现 `src/java.management.rmi/share/classes/javax/management/remote/rmi/RMIConnector.java`：`implements JMXConnector`，是标准实现。
来源均为 JDK 仓库（jdk25u-dev）源码与 `test/jdk`。

## 4. 测试用例设计
白盒：`isInterface()`、继承 `Closeable`、关键方法存在、`CREDENTIALS` 常量值。
黑盒轻行为：匿名实现验证 `connect/getConnectionId/getMBeanServerConnection/close` 可调用，`close` 后置位可观察。


---

## 深化（2026-11，批 m）：JDK-27 侧补充说明
本目录 `JMXConnectorTest.java` 在既有白盒+黑盒之上追加 `testDeepened()`，与 JDK-8 侧同源（差异仅下列各点）。

### 新增覆盖维度
- 连接生命周期 + 远程全操作闭环 + 客户端通知闭环 + close 后异常精确消息 + 未连接态。

### RMI 回环骨架（本目录批 m 实际采用）
`new JMXServiceURL("service:jmx:rmi://127.0.0.1")`（无端口、无 URL 路径）→ `JMXConnectorServerFactory.newJMXConnectorServer(url, env, server)` → `cs.start()` 后 `cs.getAddress()` 返回 `service:jmx:rmi://127.0.0.1/stub/<base64 RMIServer stub>`（stub 内嵌 URL，两侧行为一致）→ 客户端 `JMXConnectorFactory.connect(cs.getAddress(), env)`。全程 127.0.0.1 回环、零固定端口、无 LocateRegistry、无端口冲突；finally 中 `conn.close()` + `cs.stop()` 释放（不 stop 会因 RMI 非守护线程挂起 JVM）。备用 JNDI 形态 `service:jmx:rmi:///jndi/rmi://127.0.0.1:<动态端口>/jmxr`（LocateRegistry.createRegistry + ServerSocket(0) 探测）经双侧实测亦可用，但 stub 形态更干净，未采用。

### 本目录版本差异
### 全局版本差异（JDK-8 vs JDK-27，实测锁定）
1. Subject Delegation 移除：JDK-27 `JMXConnector.getMBeanServerConnection(Subject)` 抛 `UnsupportedOperationException: Subject Delegation has been removed.`（JDK-8 正常发放委托连接；未认证连接上首个远程操作抛 `SecurityException: Subject delegation cannot be enabled unless an authenticated subject is put in place`）；`SubjectDelegationPermission` 在 JDK-27 标记 `@Deprecated(forRemoval=true)` 但构造与 implies 语义不变。
2. `new JMXServiceURL((String)null)` 的 NPE 消息：JDK-8 无消息，JDK-27 为 `Cannot invoke "String.length()" because "serviceURL" is null`（测试两侧均只断言 NPE 类型）。
3. 其余异常类型与精确消息（见各目录负例表）双侧一致。
