# Notification 测试设计

## 1. API 功能
`com.sun.nio.sctp.Notification` 是接口，是所有 SCTP 通知的基类型。唯一抽象方法：

- `Association association()`

返回该通知关联的 SCTP 关联。

## 2. Java 用户如何使用
`Notification` 作为 `NotificationHandler.handleNotification(Notification, T)` 的参数与 `SctpChannel.receive`/`SctpMultiChannel.receive` 中 handler 收到的通知类型：

```java
class H extends AbstractNotificationHandler<Object> {
    @Override public HandlerResult handleNotification(Notification n, Object a) {
        Association assoc = n.association();
        return HandlerResult.CONTINUE;
    }
}
```

实际实现类如 `AssociationChangeNotification`、`ShutdownNotification` 均实现该接口。

## 3. 开源代码实际用法调研
来源（本机源码，未虚构）：

- `~/git/jdk25u-dev/src/jdk.sctp/share/classes/com/sun/nio/sctp/Notification.java`：接口定义，仅 `association()`。
- `~/git/jdk25u-dev/src/jdk.sctp/share/classes/com/sun/nio/sctp/AbstractNotificationHandler.java`：`handleNotification(Notification, T)` 默认返回 `CONTINUE`，是接口的主要消费方。
- `~/git/jdk25u-dev/src/jdk.sctp/unix/classes/sun/nio/ch/sctp/SctpNotification.java` 及其子类（`AssociationChange`、`PeerAddrChange`、`SendFailed`、`Shutdown`）实现 `Notification`，并把 native 数据映射为 `Association`。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`NotificationTest.java` 的 `testBlackBox()`）：

- 匿名实现返回固定 `Association`，断言 `association()` 精确回传、其访问器可达。
- 不同实现返回不同 association。
- 实现可合法返回 `null`（断言允许）。
- `instanceof Notification` 成立。

白盒（`testWhiteBox()`）：是接口、无父类、`association()` 返回 `Association` 且 abstract。
