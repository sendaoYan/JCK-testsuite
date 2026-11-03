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
