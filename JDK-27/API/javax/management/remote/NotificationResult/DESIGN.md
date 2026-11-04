# NotificationResult 测试设计

## 1. API 功能
`javax.management.remote.NotificationResult`（实现 `Serializable`）表示一次通知缓冲查询结果。

- 构造器：`NotificationResult(long earliestSequenceNumber, long nextSequenceNumber, TargetedNotification[] targetedNotifications)`；数组为 null 或序号为负抛 `IllegalArgumentException`。
- 查询：`getEarliestSequenceNumber()`、`getNextSequenceNumber()`、`getTargetedNotifications()`（返回内部数组的**克隆**，防御性拷贝）。
- 转换：`toString()` 形如 `NotificationResult: earliest=<e>; next=<n>; nnotifs=<len>`。

## 2. Java 用户如何使用
```java
NotificationResult nr = new NotificationResult(10L, 25L, new TargetedNotification[]{ tn });
nr.getEarliestSequenceNumber();      // 10
nr.getTargetedNotifications().length; // 1
```
典型组合链路：客户端调用 `RMIConnection.fetchNotifications(cursor, max, timeout)` 得到 `NotificationResult`，遍历 `getTargetedNotifications()` 处理每条 `TargetedNotification`，再用 `getNextSequenceNumber()` 作为下次查询的游标。

## 3. 开源/源码实际用法调研
- `test/jdk/javax/management/remote/mandatory/notif/NotificationBufferTest.java`（JDK 自带测试）：构造/接收 `NotificationResult`，多次调用 `getTargetedNotifications()` 并校验 `length` 与各 `TargetedNotification.getNotification()`。
- JDK 实现 `src/java.management/share/classes/javax/management/remote/NotificationResult.java`：`getTargetedNotifications()` 返回 `clone()`（非空时），`validate()` 对 null 抛 `Notifications null`、负序号抛 `Bad sequence numbers`。
来源均为 JDK 仓库（jdk25u-dev）源码与 `test/jdk`。

## 4. 测试用例设计
黑盒部分（`NotificationResultTest.testBlackBox()`）：
- 正常路径：三字段 getter + `toString()` 精确匹配。
- 组合/边界：`getTargetedNotifications()` 返回防御性拷贝（改返回数组不影响内部状态）；空数组往返。
- 异常（精确 message）：null 数组 → `Notifications null`；负 `earliest`/`next` → `Bad sequence numbers`。
