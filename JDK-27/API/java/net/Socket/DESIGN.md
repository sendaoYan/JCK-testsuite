# java.net.Socket 测试设计案（JDK-27）

> 覆盖目标：未连接语义 + close/状态不变量（**不真联网/不 bind 固定端口**）。
> `new Socket()`/`new Socket(Proxy.NO_PROXY)` 初始状态、SocketAddress/端口访问器（close 前后）、
> 未连接/已关闭 I/O 的精确 SocketException、SO_TIMEOUT/TCP_NODELAY/KEEPALIVE/REUSEADDR/TRAFFIC_CLASS/LINGER
> 选项往返、TRAFFIC_CLASS 越界→IAE、临时端口 bind、close 幂等。

## 1. API 功能

- 构造器：`Socket()`、`Socket(Proxy)`（其余 connect 类构造器需联网，不测）。
- 状态：`isConnected/isBound/isClosed/isInputShutdown/isOutputShutdown`、`getChannel`。
- 访问：`getInetAddress/getLocalAddress/getPort/getLocalPort/getRemoteSocketAddress/getLocalSocketAddress`。
- 选项：`set/getSoTimeout`、`set/getTcpNoDelay`、`set/getKeepAlive`、`set/getReuseAddress`、
  `set/getTrafficClass`、`set/getSoLinger`、`bind`。

## 2. 用户如何使用

- `new Socket()` 得到未连接套接字，再 `connect`；关闭用 `close()`（幂等）。
- 未连接/已关闭状态下 I/O 操作抛 SocketException。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/net/Socket.html）。
- OpenJDK `~/git/jdk8u-dev/jdk/test/java/net/Socket/`（AddressTest、TestAfterClose、TestClose、NullHost、SetSoLinger、TrafficClass）。
- JCK runtime-8b `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_net/Socket/`。

## 4. 测试用例设计（映射到 SocketTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 未连接初始状态（地址/端口/通道/shutdown/toString） | testUnconnectedState |
| P0 | NO_PROXY / SOCKS 代理未连接状态 | testNoProxyUnconnected |
| P0 | 选项往返 + TRAFFIC_CLASS 越界→IAE + LINGER | testOptions |
| P0 | 未连接 I/O：getInput/getOutput/shutdown/sendUrgentData 抛异常 | testUnconnectedIO |
| P0 | close 后状态 + 幂等 + 已关闭 I/O 抛 SocketException | testCloseSemantics |
| P1 | bind 临时端口 + bind(null)→wildcard 临时端口 | testEphemeralBind |

## 易错规范细节（已锁入断言）

- 未连接 `getPort()`=0、`getLocalPort()`=-1、`getLocalAddress().isAnyLocalAddress()`、`getChannel()`=null。
- `getInputStream/getOutputStream/shutdownInput/shutdownOutput` 未连接→SocketException；已关闭→SocketException。
- `sendUrgentData` 未连接→IOException。
- `setTrafficClass` 值须 0..255，越界→IllegalArgumentException。
- `bind(null)` 绑定 wildcard 临时端口（不是 no-op）；`bind(new InetSocketAddress(0))` 分配临时端口。
- `Proxy(Type.DIRECT, …)` 无法通过公开构造器构造（仅 Proxy.NO_PROXY）。

## JDK-27 补充

- `setOption/getOption/supportedOptions`（JDK 9+）：`getOption(null)`→NPE；`setOption(name,null)`→IAE。
- `Socket` 实现 Closeable（白盒）。

## 批次 F 深化补充（Socket 状态机簇）

> 完整状态机覆盖：回环连接对、shutdown 双向语义、close 状态机、失败后状态演化、JDK 9+ typed option API。SocketTest.java 深化至 284 断言。

### 新增测试方法

- 回环连接对基建（daemon acceptor + `ArrayBlockingQueue` 同步）；端口互易断言。
- shutdown 语义：shutdownInput→EOF、shutdownOutput→write 抛 IOException("Socket closed")。
- close 状态机：isClosed=true 且 isConnected/isBound 保持；访问器级消息 "Socket is closed"、connect 级 "Socket closed"。
- connect 失败矩阵：refused→ConnectException("Connection refused (connect failed)")；已连接再 connect→IAE("Already connected")；sendUrgentData 分级消息（未连接 "Not connected"、关闭 "Socket is closed"、impl 失活 "Socket closed"）。
- `testConnectedSocketOptionApi`：setOption/getOption(SO_LINGER) 往返；SO_BROADCAST→UnsupportedOperationException。
- trafficClass 精确回读（JDK-27 无 JDK-8 的 0xFC 掩码）；refused 后 isClosed=true（JDK-8 为 false）。
