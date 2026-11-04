# java.net.SocketAddress 测试设计案（JDK-8）

> 批次 F 深化：抽象值类型契约、自定义实现、序列化 round-trip、bind 对外来实现的拒绝。

## 1. API 功能

- 抽象类 `SocketAddress`（无抽象方法的标记基类，implements Serializable）。
- 平台实现：`InetSocketAddress`（端口 + 地址/主机名，解析/未解析两态）。
- 消费方：`Socket.bind/connect`、`ServerSocket.bind` 均收 SocketAddress 参数。

## 2. 测试用例设计（SocketAddressTest.java，46 断言）

- 自定义 `PlainAddress` 子类：可实例化、可序列化、equals/hashCode 自治。
- `InetSocketAddress` 组合：构造矩阵（port+ InetAddress、port+host、port 0）、越界端口→IAE（负数、>65535）。
- 序列化 round-trip：未解析地址（按主机名构造）保持未解析语义（getHostName 可用、getAddress 可能为 null）。
- `Socket.bind(外来实现)`→SocketException "Unsupported address type"（平台只认 InetSocketAddress）。
- equals/hashCode：InetSocketAddress 按地址+端口组合等值。

## 易错规范细节（已锁入断言）

- SocketAddress 自身没有声明抽象方法，子类只需实现序列化兼容。
- bind/connect 传入非 InetSocketAddress 的实现 → SocketException（消息含 "Unsupported address type"）。

## JDK-8 vs JDK-27 差异

- JDK-27（52 断言）另有 `UnixDomainSocketAddress`（JDK 16+）：equals 按 path、path 可含任意字符。
