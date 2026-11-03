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
