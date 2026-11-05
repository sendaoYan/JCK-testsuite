# JMXConnectionNotification 测试设计

## 1. API 功能
`javax.management.remote.JMXConnectionNotification`（继承 `Notification`）表示客户端连接生命周期事件。

- 常量：`OPENED`/`CLOSED`/`FAILED`/`NOTIFS_LOST`（`jmx.remote.connection.*`）。
- 构造器：`JMXConnectionNotification(String type, Object source, String connectionId, long sequenceNumber, String message, Object userData)`；type/source/connectionId 为 null 抛 `NullPointerException`，负序号抛 `IllegalArgumentException`。
- 查询：继承的 `getType/getSource/getSequenceNumber/getMessage/getUserData` + `getConnectionId()`。

## 2. Java 用户如何使用
```java
JMXConnectionNotification n = new JMXConnectionNotification(
    JMXConnectionNotification.OPENED, source, "conn-7", 1L, "opened", extra);
n.getConnectionId(); // "conn-7"
n.getType();         // JMXConnectionNotification.OPENED
```
典型组合链路：连接器服务器/客户端在 `connectionOpened/connectionClosed/connectionFailed` 回调里构造该通知并广播；监听器按 `getType()` 匹配四种常量。

## 3. 开源/源码实际用法调研
- JDK 实现 `src/java.management/share/classes/javax/management/remote/JMXConnectorServer.java`：`connectionOpened/Closed/Failed` 三个受保护方法内部构造 `JMXConnectionNotification`，`getNotificationInfo()` 声明三种类型。
- JDK 实现 `src/java.management/share/classes/javax/management/remote/rmi/RMIConnector.java`：客户端连接状态变化时发送 `JMXConnectionNotification`。
来源均为 JDK 仓库（jdk25u-dev）源码。

## 4. 测试用例设计
黑盒部分（`JMXConnectionNotificationTest.testBlackBox()`）：
- 正常路径：四个常量精确值；构造后各 getter（含 `getConnectionId`）正确、`getSource` 保留同一实例。
- 组合/边界：CLOSED 通知 null message/null userData 允许。
- 异常（精确 message）：null type/source/connectionId → `Illegal null argument`（NPE）；负序号 → `Negative sequence number`（IAE）。


---

## 深化（2026-11，批 m）：JDK-27 侧补充说明
本目录 `JMXConnectionNotificationTest.java` 在既有白盒+黑盒之上追加 `testDeepened()`，与 JDK-8 侧同源（差异仅下列各点）。

### 新增覆盖维度
- 常量 + 构造矩阵 + 序列化 + RMI 真触发（type/id/消息/序号）。

### RMI 回环骨架（本目录批 m 实际采用）
`new JMXServiceURL("service:jmx:rmi://127.0.0.1")`（无端口、无 URL 路径）→ `JMXConnectorServerFactory.newJMXConnectorServer(url, env, server)` → `cs.start()` 后 `cs.getAddress()` 返回 `service:jmx:rmi://127.0.0.1/stub/<base64 RMIServer stub>`（stub 内嵌 URL，两侧行为一致）→ 客户端 `JMXConnectorFactory.connect(cs.getAddress(), env)`。全程 127.0.0.1 回环、零固定端口、无 LocateRegistry、无端口冲突；finally 中 `conn.close()` + `cs.stop()` 释放（不 stop 会因 RMI 非守护线程挂起 JVM）。备用 JNDI 形态 `service:jmx:rmi:///jndi/rmi://127.0.0.1:<动态端口>/jmxr`（LocateRegistry.createRegistry + ServerSocket(0) 探测）经双侧实测亦可用，但 stub 形态更干净，未采用。

### 本目录版本差异
### 全局版本差异（JDK-8 vs JDK-27，实测锁定）
1. Subject Delegation 移除：JDK-27 `JMXConnector.getMBeanServerConnection(Subject)` 抛 `UnsupportedOperationException: Subject Delegation has been removed.`（JDK-8 正常发放委托连接；未认证连接上首个远程操作抛 `SecurityException: Subject delegation cannot be enabled unless an authenticated subject is put in place`）；`SubjectDelegationPermission` 在 JDK-27 标记 `@Deprecated(forRemoval=true)` 但构造与 implies 语义不变。
2. `new JMXServiceURL((String)null)` 的 NPE 消息：JDK-8 无消息，JDK-27 为 `Cannot invoke "String.length()" because "serviceURL" is null`（测试两侧均只断言 NPE 类型）。
3. 其余异常类型与精确消息（见各目录负例表）双侧一致。
