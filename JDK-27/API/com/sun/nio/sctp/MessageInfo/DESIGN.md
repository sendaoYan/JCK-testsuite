# MessageInfo 测试设计

## 1. API 功能
`com.sun.nio.sctp.MessageInfo` 是抽象类，提供消息的辅助信息。构造器 `protected MessageInfo()`，两个静态工厂：

- `static MessageInfo createOutgoing(SocketAddress address, int streamNumber)`
- `static MessageInfo createOutgoing(Association association, SocketAddress address, int streamNumber)`

查询/设置方法（均为 abstract）：

- `SocketAddress address()`、`Association association()`、`int bytes()`
- `boolean isComplete()` / `MessageInfo complete(boolean)`
- `boolean isUnordered()` / `MessageInfo unordered(boolean)`
- `int payloadProtocolID()` / `MessageInfo payloadProtocolID(int)`
- `int streamNumber()` / `MessageInfo streamNumber(int)`
- `long timeToLive()` / `MessageInfo timeToLive(long)`

## 2. Java 用户如何使用
发送消息前构造 outgoing info，并可链式设置：

```java
MessageInfo mi = MessageInfo.createOutgoing(addr, 5);
mi.unordered(true).payloadProtocolID(7).timeToLive(1000L);
channel.send(buffer, mi);
```

默认值（源码 `sun.nio.ch.sctp.MessageInfoImpl`）：`complete=true`、`unordered=false`、`timeToLive=0L`、`ppid=0`、`bytes=0`。流号有效范围 `[0, 65536]`，越界抛 `IllegalArgumentException`；三参工厂 `association == null` 也抛 `IllegalArgumentException`。

## 3. 开源代码实际用法调研
来源（本机源码，未虚构）：

- `~/git/jdk25u-dev/test/jdk/com/sun/nio/sctp/MessageInfoTests.java`（已完整阅读）：`checkDefaults` 断言默认值 `unordered=false / timeToLive=0L / complete=true / ppid=0 / bytes=0`；`checkGetterSetters` 验证各 setter 后 getter 回读；`testIAE` 覆盖 `createOutgoing(addr,-1)`、`createOutgoing(addr,65537)`、`createOutgoing(null,addr,0)`、`createOutgoing(assoc,addr,-1)`、`createOutgoing(assoc,addr,65537)`、`streamNumber(-1)`、`streamNumber(65537)`。
- `~/git/jdk25u-dev/src/jdk.sctp/share/classes/com/sun/nio/sctp/MessageInfo.java`：工厂与默认值文档。
- `~/git/jdk25u-dev/src/jdk.sctp/share/classes/sun/nio/ch/sctp/MessageInfoImpl.java`：字段默认值与 `streamNumber` 校验实现。
- JDK 测试 `~/git/jdk25u-dev/test/jdk/com/sun/nio/sctp/SctpChannel/Send.java`、`SctpMultiChannel/Send.java`：实际用 `MessageInfo.createOutgoing(...)` 后 `send`。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`MessageInfoTest.java` 的 `testBlackBox()`）：

- 二参工厂默认值全部断言（address/association/streamNumber/bytes/isComplete/isUnordered/ppid/ttl）。
- 链式 setter 返回 this 且状态回读（complete/unordered/ppid/streamNumber/ttl）。
- 三参工厂携带 association；两/三参工厂均允许 `address == null`。
- 流号边界：`-1`、`65537` 抛 `IllegalArgumentException`，`0`、`65536` 接受；三参工厂 `association == null` 抛 IAE；setter 同边界。

白盒（`testWhiteBox()`）：抽象类、protected 无参构造器、两个静态工厂存在、关键方法集合存在。
