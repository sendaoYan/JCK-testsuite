# SctpServerChannel 测试设计

## 1. API 功能
`com.sun.nio.sctp.SctpServerChannel` 是抽象类，继承 `AbstractSelectableChannel`，表示 SCTP 服务端通道。静态工厂：

- `static SctpServerChannel open()`

主要操作：`accept()`、`bind(SocketAddress)`（final，委托 `bind(local,0)`）与 `bind(SocketAddress,int)`、`bindAddress`/`unbindAddress`、`getAllLocalAddresses`、`getOption`/`setOption`、`supportedOptions`、`validOps()`（final，返回 `OP_ACCEPT`）。

## 2. Java 用户如何使用
```java
SctpServerChannel ss = SctpServerChannel.open();
ss.bind(addr);
SctpChannel accepted = ss.accept();
ss.close();
```

无内核 SCTP 支持时 `open()` 抛 `UnsupportedOperationException`。

## 3. 开源代码实际用法调研
来源（本机源码，未虚构）：

- `~/git/jdk25u-dev/src/jdk.sctp/share/classes/com/sun/nio/sctp/SctpServerChannel.java`（已读 `open`/`bind`/`validOps`）：`open()` 委托 `SctpServerChannelImpl(null)`；`validOps()` 返回 `OP_ACCEPT`；`bind(SocketAddress)` 为 `bind(local, 0)`。
- `~/git/jdk25u-dev/src/jdk.sctp/share/classes/sun/nio/ch/sctp/SctpServerChannelImpl.java`：不支持平台 fallback，抛 `UnsupportedOperationException`。
- `~/git/jdk25u-dev/src/jdk.sctp/unix/classes/sun/nio/ch/sctp/SctpServerChannelImpl.java`：Linux 原生实现。
- JDK 测试 `~/git/jdk25u-dev/test/jdk/com/sun/nio/sctp/SctpServerChannel/*.java`（Accept/NonBlockingAccept/Util）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`SctpServerChannelTest.java` 的 `testBlackBox()`）：

- 尝试 `open()`：成功则断言非空、`isOpen()`、默认阻塞、`validOps()==OP_ACCEPT`、`supportedOptions()!=null`，随后 `close()`。
- 抛 `UnsupportedOperationException` 视为合法降级路径。
- 第二次 `open()` 行为一致。

白盒（`testWhiteBox()`）：抽象类、继承 `AbstractSelectableChannel`、方法集合、`validOps` final、`open()` static。
