# SendFailedNotification 测试设计

## 1. API 功能
`com.sun.nio.sctp.SendFailedNotification` 是抽象类，实现 `Notification`，表示发送失败通知。抽象方法：

- `Association association()`
- `SocketAddress address()`
- `ByteBuffer buffer()`
- `int errorCode()`
- `int streamNumber()`

构造器 `protected SendFailedNotification()`。

## 2. Java 用户如何使用
```java
@Override public HandlerResult handleNotification(SendFailedNotification n, Object a) {
    ByteBuffer failed = n.buffer();     // 未发送出的数据
    int code = n.errorCode();           // 错误码
    int stream = n.streamNumber();
    return HandlerResult.CONTINUE;
}
```

`buffer()` 返回导致失败的消息缓冲，允许重发。

## 3. 开源代码实际用法调研
来源（本机源码，未虚构）：

- `~/git/jdk25u-dev/src/jdk.sctp/share/classes/com/sun/nio/sctp/SendFailedNotification.java`：抽象方法集合。
- `~/git/jdk25u-dev/src/jdk.sctp/unix/classes/sun/nio/ch/sctp/SendFailed.java`：内部实现，持有失败消息缓冲与错误码/流号。
- `~/git/jdk25u-dev/test/jdk/com/sun/nio/sctp/SctpMultiChannel/SendFailed.java`：覆盖 `handleNotification(SendFailedNotification,...)` 校验发送失败路径。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`SendFailedNotificationTest.java` 的 `testBlackBox()`）：

- 匿名实现五个方法精确回传（association/address/buffer/errorCode/streamNumber）。
- 负 errorCode/streamNumber 与 null 字段仅作为数据透传（无校验）。

白盒（`testWhiteBox()`）：抽象类、实现 `Notification`、protected 构造器、五方法存在、`buffer` 返回 `ByteBuffer`、`errorCode`/`streamNumber` 返回 `int`。
