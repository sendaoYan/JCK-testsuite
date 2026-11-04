# NotificationHandler 测试设计

## 1. API 功能
`com.sun.nio.sctp.NotificationHandler<T>` 是泛型接口（函数式接口），唯一抽象方法：

- `HandlerResult handleNotification(Notification notification, T attachment)`

在 `SctpChannel.receive`/`SctpMultiChannel.receive` 收到通知时被回调，`attachment` 由发起 receive 时附加。

## 2. Java 用户如何使用
```java
NotificationHandler<String> h = (n, attachment) -> HandlerResult.CONTINUE;
channel.receive(buffer, "my-attachment", h);
```

返回 `CONTINUE` 继续接收，`RETURN` 结束一次 receive。

## 3. 开源代码实际用法调研
来源（本机源码，未虚构）：

- `~/git/jdk25u-dev/src/jdk.sctp/share/classes/com/sun/nio/sctp/NotificationHandler.java`：接口定义。
- `~/git/jdk25u-dev/src/jdk.sctp/share/classes/com/sun/nio/sctp/AbstractNotificationHandler.java`：实现该接口，为每个通知类型提供 `CONTINUE` 默认实现。
- `~/git/jdk25u-dev/test/jdk/com/sun/nio/sctp/SctpChannel/Receive.java`：用 `NotificationHandler`（常结合 `AbstractNotificationHandler`）接收消息并消费通知。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`NotificationHandlerTest.java` 的 `testBlackBox()`）：

- lambda 实现返回 `CONTINUE` / `RETURN` 分别断言。
- `null` notification/attachment 可被调用且返回预期。
- 捕获 attachment 验证透传。
- `instanceof NotificationHandler`。

白盒（`testWhiteBox()`）：接口、一个泛型类型参数、`handleNotification` 返回 `HandlerResult` 且 abstract。
