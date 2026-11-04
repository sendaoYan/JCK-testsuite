# SctpChannel 测试设计

## 1. API 功能
`com.sun.nio.sctp.SctpChannel` 是抽象类，继承 `java.nio.channels.spi.AbstractSelectableChannel`，表示一个（已连接）SCTP 通道。静态工厂：

- `static SctpChannel open()`（无参，本测试黑盒使用）
- `static SctpChannel open(SocketAddress remote, int maxOutStreams, int maxInStreams)`

主要操作：`association()`、`bind`/`bindAddress`/`unbindAddress`、`connect`、`isConnectionPending`/`finishConnect`、`getAllLocalAddresses`/`getRemoteAddresses`、`shutdown`、`getOption`/`setOption`/`supportedOptions`、`validOps()`（final，返回 `OP_READ|OP_WRITE|OP_CONNECT`）、`receive`、`send`。

## 2. Java 用户如何使用
```java
SctpChannel ch = SctpChannel.open();      // 或 open(remote, 0, 0)
ch.connect(remote, 0, 0);
ch.send(buffer, MessageInfo.createOutgoing(remote, 0));
ch.receive(buf, attachment, handler);
ch.close();
```

无内核 SCTP 支持时 `open()` 抛出 `UnsupportedOperationException`（文档化降级路径）。

## 3. 开源代码实际用法调研
来源（本机源码，未虚构）：

- `~/git/jdk25u-dev/src/jdk.sctp/share/classes/com/sun/nio/sctp/SctpChannel.java`（已读 `open`/`validOps`）：`open()` 委托 `new sun.nio.ch.sctp.SctpChannelImpl(null)`；`validOps()` 返回 `OP_READ|OP_WRITE|OP_CONNECT`。
- `~/git/jdk25u-dev/src/jdk.sctp/share/classes/sun/nio/ch/sctp/SctpChannelImpl.java`：不支持平台的 fallback 实现，所有方法抛 `UnsupportedOperationException("SCTP not supported...")`。
- `~/git/jdk25u-dev/src/jdk.sctp/unix/classes/sun/nio/ch/sctp/SctpChannelImpl.java`：Linux 原生实现。
- JDK 测试 `~/git/jdk25u-dev/test/jdk/com/sun/nio/sctp/SctpChannel/*.java`（Connect/Bind/Send/Receive/Shutdown/SocketOptionTests/CloseDescriptors）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`SctpChannelTest.java` 的 `testBlackBox()`）：

- 尝试 `open()`：成功则断言非空、`isOpen()`、默认阻塞、`validOps()==OP_READ|OP_WRITE|OP_CONNECT`、`supportedOptions()!=null`，随后 `close()` 并断言已关闭。
- 抛 `UnsupportedOperationException` 则视为合法降级路径（不绑端口、不真实收发）。
- 第二次 `open()` 与第一次行为一致（确定性）。

白盒（`testWhiteBox()`）：抽象类、继承 `AbstractSelectableChannel`、方法集合、`validOps` final、`open()`/`open(SocketAddress,int,int)` static。
