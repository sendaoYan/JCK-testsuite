# HandlerResult 测试设计

## 1. API 功能
`com.sun.nio.sctp.HandlerResult` 是一个枚举（`enum`，继承 `java.lang.Enum<HandlerResult>`），表达 `NotificationHandler` 处理完一个 SCTP 通知后的决策：

- 常量：`CONTINUE`（继续接收/处理）、`RETURN`（停止处理并返回）。
- 继承方法：`values()`、`valueOf(String)`、`name()`、`ordinal()`、`toString()`。

JDK-27 javap 确认：仅两个常量，无额外成员方法。

## 2. Java 用户如何使用
```java
HandlerResult r = HandlerResult.CONTINUE;
switch (r) { case CONTINUE: ...; case RETURN: ...; }
HandlerResult.valueOf("RETURN");
HandlerResult.values();      // [CONTINUE, RETURN]
```

典型链路：自定义 `NotificationHandler` 返回 `CONTINUE` 继续，或 `RETURN` 终止一次 `receive` 循环。

## 3. 开源代码实际用法调研
来源（未虚构，均为本机源码）：

- `~/git/jdk25u-dev/src/jdk.sctp/share/classes/com/sun/nio/sctp/AbstractNotificationHandler.java`：默认实现每个 `handleNotification` 都 `return HandlerResult.CONTINUE;`（消费通知并继续）。
- `~/git/jdk25u-dev/src/jdk.sctp/share/classes/sun/nio/ch/sctp/ResultContainer.java`：把本地回调结果映射为 `HandlerResult`。
- JDK 测试 `~/git/jdk25u-dev/test/jdk/com/sun/nio/sctp/SctpChannel/Receive.java` 等，通过 handler 返回 `CONTINUE` 反复接收直到消息到达。

## 4. 测试用例设计 + 自动化实现
黑盒场景（见 `HandlerResultTest.java` 的 `testBlackBox()`）：

- 两个常量的 `ordinal()` 顺序（CONTINUE=0, RETURN=1）。
- `valueOf`/`values`/`name` 的往返与边界（`valueOf("BOGUS")` 抛 `IllegalArgumentException`）。
- `switch` 分发两个常量。
- `toString()` 等于枚举常量名。

白盒（`testWhiteBox()`）：`isEnum`、父类为 `java.lang.Enum`、`values`/`valueOf` 方法存在、常量名正确。
