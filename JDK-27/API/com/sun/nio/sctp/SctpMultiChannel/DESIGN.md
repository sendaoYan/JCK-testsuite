# SctpMultiChannel 测试设计

## 1. API 功能
`com.sun.nio.sctp.SctpMultiChannel` 是抽象类，继承 `AbstractSelectableChannel`，表示一个可同时关联多个 SCTP 关联的多通道。静态工厂：

- `static SctpMultiChannel open()`

主要操作：`associations()`、`bind(SocketAddress,int)`（及 final `bind(SocketAddress)`）、`bindAddress`/`unbindAddress`、`getAllLocalAddresses`/`getRemoteAddresses(Association)`、`shutdown(Association)`、`getOption`/`setOption`、`supportedOptions`、`validOps()`（final，返回 `OP_READ|OP_WRITE`）、`receive`、`send`、`branch(Association)`。

## 2. Java 用户如何使用
```java
SctpMultiChannel ch = SctpMultiChannel.open();
ch.bind(addr);
Set<Association> assocs = ch.associations();
SctpChannel branch = ch.branch(assoc);
ch.close();
```

无内核 SCTP 支持时 `open()` 抛 `UnsupportedOperationException`。

## 3. 开源代码实际用法调研
来源（本机源码，未虚构）：

- `~/git/jdk25u-dev/src/jdk.sctp/share/classes/com/sun/nio/sctp/SctpMultiChannel.java`（已读 `open`/`validOps`）：`open()` 委托 `SctpMultiChannelImpl(null)`；`validOps()` 返回 `OP_READ|OP_WRITE`。
- `~/git/jdk25u-dev/src/jdk.sctp/share/classes/sun/nio/ch/sctp/SctpMultiChannelImpl.java`：不支持平台 fallback，抛 `UnsupportedOperationException`。
- `~/git/jdk25u-dev/src/jdk.sctp/unix/classes/sun/nio/ch/sctp/SctpMultiChannelImpl.java`：Linux 原生实现。
- JDK 测试 `~/git/jdk25u-dev/test/jdk/com/sun/nio/sctp/SctpMultiChannel/*.java`（Branch/CloseDescriptors/Send/SendFailed/SocketOptionTests）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`SctpMultiChannelTest.java` 的 `testBlackBox()`）：

- 尝试 `open()`：成功则断言非空、`isOpen()`、默认阻塞、`validOps()==OP_READ|OP_WRITE`、`supportedOptions()!=null`，随后 `close()`。
- 抛 `UnsupportedOperationException` 视为合法降级路径。
- 第二次 `open()` 行为一致。

白盒（`testWhiteBox()`）：抽象类、继承 `AbstractSelectableChannel`、方法集合、`validOps` final、`open()` static。
