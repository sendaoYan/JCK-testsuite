# TargetedNotification 测试设计

## 1. API 功能
`javax.management.remote.TargetedNotification`（实现 `Serializable`）把一个 `Notification` 与其目标监听器 ID 打包，供通知缓冲跨连接传输。

- 构造器：`TargetedNotification(Notification notification, Integer listenerID)`；二者任一为 null 抛 `IllegalArgumentException`。
- 查询：`getNotification()`（返回原通知实例）、`getListenerID()`（返回 `Integer`）。
- 转换：`toString()` 形如 `{<notification.toString()>, <id>}`。

## 2. Java 用户如何使用
```java
TargetedNotification tn = new TargetedNotification(new Notification("type", "src", 1L), 5);
tn.getNotification().getType();  // "type"
tn.getListenerID().intValue();   // 5
```
典型组合链路：服务器端 `NotificationBuffer.fetchNotifications` 返回 `NotificationResult`，其中每个元素是 `TargetedNotification`；客户端遍历 `getTargetedNotifications()`，再按 `getListenerID()` 分发并读 `getNotification()`。

## 3. 开源/源码实际用法调研
- `test/jdk/javax/management/remote/mandatory/notif/NotificationBufferTest.java`（JDK 自带测试，104–317 行）：反复调用 `nr.getTargetedNotifications()`，对每个 `TargetedNotification` 取 `getNotification()` 校验序列号与类型，是 `NotificationResult` + `TargetedNotification` 组合的权威用例。
- JDK 实现 `src/java.management/share/classes/javax/management/remote/TargetedNotification.java`：`toString()` 返回 `"{" + notif + ", " + id + "}"`，`validate()` 对 null 分别抛 `Invalid notification: null` / `Invalid listener ID: null`。
来源均为 JDK 仓库（jdk25u-dev）源码与 `test/jdk`。

## 4. 测试用例设计
黑盒部分（`TargetedNotificationTest.testBlackBox()`）：
- 正常路径：构造后断言 `getNotification()` 为同一实例、类型/序列号/监听器 ID 正确。
- 组合：`toString()` 与 `Notification` 的 `toString()` 拼接一致。
- 异常（精确 message）：null 通知 → `Invalid notification: null`；null ID → `Invalid listener ID: null`。
- 边界：两个内容相同的实例是不同对象（身份独立）。


---

## 深化（2026-11，批 m）：JDK-27 侧补充说明
本目录 `TargetedNotificationTest.java` 在既有白盒+黑盒之上追加 `testDeepened()`，与 JDK-8 侧同源（差异仅下列各点）。

### 新增覆盖维度
- 恒等 getter + null 矩阵 + 恒等语义 + 序列化。

### RMI 回环骨架（本目录批 m 实际采用）
`new JMXServiceURL("service:jmx:rmi://127.0.0.1")`（无端口、无 URL 路径）→ `JMXConnectorServerFactory.newJMXConnectorServer(url, env, server)` → `cs.start()` 后 `cs.getAddress()` 返回 `service:jmx:rmi://127.0.0.1/stub/<base64 RMIServer stub>`（stub 内嵌 URL，两侧行为一致）→ 客户端 `JMXConnectorFactory.connect(cs.getAddress(), env)`。全程 127.0.0.1 回环、零固定端口、无 LocateRegistry、无端口冲突；finally 中 `conn.close()` + `cs.stop()` 释放（不 stop 会因 RMI 非守护线程挂起 JVM）。备用 JNDI 形态 `service:jmx:rmi:///jndi/rmi://127.0.0.1:<动态端口>/jmxr`（LocateRegistry.createRegistry + ServerSocket(0) 探测）经双侧实测亦可用，但 stub 形态更干净，未采用。

### 本目录版本差异
### 全局版本差异（JDK-8 vs JDK-27，实测锁定）
1. Subject Delegation 移除：JDK-27 `JMXConnector.getMBeanServerConnection(Subject)` 抛 `UnsupportedOperationException: Subject Delegation has been removed.`（JDK-8 正常发放委托连接；未认证连接上首个远程操作抛 `SecurityException: Subject delegation cannot be enabled unless an authenticated subject is put in place`）；`SubjectDelegationPermission` 在 JDK-27 标记 `@Deprecated(forRemoval=true)` 但构造与 implies 语义不变。
2. `new JMXServiceURL((String)null)` 的 NPE 消息：JDK-8 无消息，JDK-27 为 `Cannot invoke "String.length()" because "serviceURL" is null`（测试两侧均只断言 NPE 类型）。
3. 其余异常类型与精确消息（见各目录负例表）双侧一致。
