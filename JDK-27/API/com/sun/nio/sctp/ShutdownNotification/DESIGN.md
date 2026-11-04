# ShutdownNotification 测试设计

## 1. API 功能
`com.sun.nio.sctp.ShutdownNotification` 是抽象类，实现 `Notification`，表示对端关闭关联的通知。唯一抽象方法：

- `Association association()`

构造器 `protected ShutdownNotification()`。

## 2. Java 用户如何使用
```java
@Override public HandlerResult handleNotification(ShutdownNotification n, Object a) {
    Association assoc = n.association();
    return HandlerResult.RETURN;  // 收到关闭通知后结束 receive
}
```

## 3. 开源代码实际用法调研
来源（本机源码，未虚构）：

- `~/git/jdk25u-dev/src/jdk.sctp/share/classes/com/sun/nio/sctp/ShutdownNotification.java`：抽象类 + `association()`。
- `~/git/jdk25u-dev/src/jdk.sctp/unix/classes/sun/nio/ch/sctp/Shutdown.java`：内部实现，映射 native SHUTDOWN 事件。
- `~/git/jdk25u-dev/test/jdk/com/sun/nio/sctp/SctpChannel/Shutdown.java`：使用 `AbstractNotificationHandler` 覆盖 `handleNotification(ShutdownNotification,...)` 检测关闭。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`ShutdownNotificationTest.java` 的 `testBlackBox()`）：

- 匿名实现 `association()` 精确回传、访问器可达、`instanceof Notification`。
- 实现可返回 null association。

白盒（`testWhiteBox()`）：抽象类、实现 `Notification`、protected 构造器、`association()` 返回 `Association`。
