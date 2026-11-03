# AbstractNotificationHandler 测试设计

## 1. API 功能
`com.sun.nio.sctp.AbstractNotificationHandler<T>` 是实现 `NotificationHandler<T>` 的骨架类（具体类，泛型），提供 5 个 `handleNotification` 重载：

- `handleNotification(Notification, T)`
- `handleNotification(AssociationChangeNotification, T)`
- `handleNotification(PeerAddressChangeNotification, T)`
- `handleNotification(SendFailedNotification, T)`
- `handleNotification(ShutdownNotification, T)`

构造器 `protected AbstractNotificationHandler()`。所有默认实现均返回 `HandlerResult.CONTINUE`（消费通知并继续），子类可覆盖单个重载返回 `RETURN`。

## 2. Java 用户如何使用
```java
AbstractNotificationHandler<Object> h = new AbstractNotificationHandler<>() {
    @Override public HandlerResult handleNotification(ShutdownNotification n, Object a) {
        return HandlerResult.RETURN;
    }
};
channel.receive(buffer, attachment, h);
```

未覆盖的通知类型走默认 `CONTINUE`。

## 3. 开源代码实际用法调研
来源（本机源码，未虚构）：

- `~/git/jdk25u-dev/src/jdk.sctp/share/classes/com/sun/nio/sctp/AbstractNotificationHandler.java`（已完整阅读）：5 个重载全部 `return HandlerResult.CONTINUE;`，类注释明确“consume notifications and continues”。
- `~/git/jdk25u-dev/test/jdk/com/sun/nio/sctp/SctpChannel/CommUp.java`、`SctpMultiChannel/SendFailed.java`：继承 `AbstractNotificationHandler` 覆盖特定通知类型。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`AbstractNotificationHandlerTest.java` 的 `testBlackBox()`）：

- 默认实现 5 个重载均返回 `CONTINUE`（含 null 参数）。
- 非 null attachment 被忽略但仍返回 `CONTINUE`。
- 具体 `Notification` 实例走泛型重载返回 `CONTINUE`。
- 子类覆盖 `ShutdownNotification` 返回 `RETURN`，其余重载仍 `CONTINUE`（验证默认不被破坏）。

白盒（`testWhiteBox()`）：具体类、泛型、实现 `NotificationHandler`、protected 无参构造器、恰好 5 个 `handleNotification` 重载。
