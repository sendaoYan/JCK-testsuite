# AssociationChangeNotification 测试设计

## 1. API 功能
`com.sun.nio.sctp.AssociationChangeNotification` 是抽象类，实现 `Notification`，表示关联状态变化通知。抽象方法：

- `Association association()`
- `AssocChangeEvent event()`

嵌套枚举 `AssocChangeEvent`：`COMM_UP`、`COMM_LOST`、`RESTART`、`SHUTDOWN`、`CANT_START`（共 5 个常量，JDK-27 javap 确认）。

构造器 `protected AssociationChangeNotification()`。

## 2. Java 用户如何使用
```java
AbstractNotificationHandler<Object> h = new AbstractNotificationHandler<>() {
    @Override public HandlerResult handleNotification(AssociationChangeNotification n, Object a) {
        AssocChangeEvent e = n.event();
        if (e == AssocChangeEvent.COMM_UP) { ... }
        return HandlerResult.CONTINUE;
    }
};
```

测试中以匿名子类覆盖 `association()`/`event()` 构造实例。

## 3. 开源代码实际用法调研
来源（本机源码，未虚构）：

- `~/git/jdk25u-dev/src/jdk.sctp/share/classes/com/sun/nio/sctp/AssociationChangeNotification.java`：抽象类 + 嵌套 `AssocChangeEvent` 枚举。
- `~/git/jdk25u-dev/src/jdk.sctp/unix/classes/sun/nio/ch/sctp/AssociationChange.java`：内部实现 `AssociationChangeNotification`，把 native 事件映射为 `AssocChangeEvent` 与 `AssociationImpl`。
- `~/git/jdk25u-dev/test/jdk/com/sun/nio/sctp/SctpChannel/CommUp.java`：覆盖 `handleNotification(AssociationChangeNotification,...)` 等待 `COMM_UP`。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`AssociationChangeNotificationTest.java` 的 `testBlackBox()`）：

- 匿名实现 `association()`/`event()` 精确回传，`instanceof Notification`。
- `AssocChangeEvent.values()` 长度 5、各常量名正确、`valueOf` 往返。
- 实现可返回 `null` association、任意事件。

白盒（`testWhiteBox()`）：抽象类、实现 `Notification`、protected 构造器、`association`/`event` 方法、`event` 返回 `AssocChangeEvent`、存在嵌套类。
