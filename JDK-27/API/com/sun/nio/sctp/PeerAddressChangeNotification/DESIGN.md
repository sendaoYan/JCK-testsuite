# PeerAddressChangeNotification 测试设计

## 1. API 功能
`com.sun.nio.sctp.PeerAddressChangeNotification` 是抽象类，实现 `Notification`，表示对端地址变化通知。抽象方法：

- `SocketAddress address()`
- `Association association()`
- `AddressChangeEvent event()`

嵌套枚举 `AddressChangeEvent`：`ADDR_AVAILABLE`、`ADDR_UNREACHABLE`、`ADDR_REMOVED`、`ADDR_ADDED`、`ADDR_MADE_PRIMARY`、`ADDR_CONFIRMED`（共 6 个常量，JDK-27 javap 确认）。

构造器 `protected PeerAddressChangeNotification()`。

## 2. Java 用户如何使用
```java
@Override public HandlerResult handleNotification(PeerAddressChangeNotification n, Object a) {
    SocketAddress addr = n.address();
    AddressChangeEvent e = n.event();
    return HandlerResult.CONTINUE;
}
```

测试中以匿名子类覆盖三个抽象方法。

## 3. 开源代码实际用法调研
来源（本机源码，未虚构）：

- `~/git/jdk25u-dev/src/jdk.sctp/share/classes/com/sun/nio/sctp/PeerAddressChangeNotification.java`：抽象类 + 嵌套 `AddressChangeEvent` 枚举。
- `~/git/jdk25u-dev/src/jdk.sctp/unix/classes/sun/nio/ch/sctp/PeerAddrChange.java`：内部实现，映射 native 地址变化事件。
- JDK 测试 `~/git/jdk25u-dev/test/jdk/com/sun/nio/sctp/SctpChannel/Connect.java` 等通过 `AbstractNotificationHandler` 消费该通知。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`PeerAddressChangeNotificationTest.java` 的 `testBlackBox()`）：

- 匿名实现 `address()`/`association()`/`event()` 精确回传。
- `AddressChangeEvent.values()` 长度 6、各常量名正确、`valueOf` 往返。
- 实现可返回 null address/association、任意事件。

白盒（`testWhiteBox()`）：抽象类、实现 `Notification`、protected 构造器、三抽象方法、`event` 返回 `AddressChangeEvent`、存在嵌套类。
