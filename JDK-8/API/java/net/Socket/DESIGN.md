# java.net.Socket 测试设计案（JDK-8）

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

## 批次 F 深化补充（Socket 状态机簇）

> 在原"未连接语义"基础上扩展为完整状态机覆盖：回环连接对（`ServerSocket(0,50,127.0.0.1)` + daemon acceptor + `ArrayBlockingQueue` 同步）、shutdown 双向语义、close 状态机、失败后的状态演化。全部回环、SoTimeout 有界（150-500ms）、finally 关闭。

### 新增测试方法（SocketTest.java，250 断言）

- `connectPair/closePair` 回环连接对基建；`testLoopbackConnectPair` 建立后端口互易（getPort==对端 localPort）。
- `testShutdownSemantics`：shutdownInput 后 read→EOF(-1)、对端不受影响；shutdownOutput 后 write→IOException("Socket closed")；标志位单向不可逆。
- `testCloseStateMachine`：close 后 isClosed=true 但 isConnected/isBound 保持 true；重复 close 幂等；关闭 socket 所有访问器/IO→"Socket is closed"。
- `testConnectFailures`： refused 回环端口→ConnectException；超时→SocketTimeoutException；已连接再 connect→IllegalArgumentException("Already connected")；closed 后 connect→SocketException("Socket closed")。
- `testBindFailures`：bind 已占用端口→BindException；已连接再 bind、closed 后 bind→异常矩阵。
- `testOptionMatrix`：负例矩阵（负 SoTimeout→IAE、负 buffer→IAE、越界 trafficClass→IAE、两参 getSoLinger(-1)）+ 正例往返。
- `testLiveOptionRoundTrips`：连接态 TCP_NODELAY/SO_TIMEOUT/SO_LINGER/OOBINLINE 往返。
- `testUrgentData`：未连接→IOException；已连接成功；closed→IOException（JDK-8 为 "Broken pipe (Write failed)"，仅断言类型）。
- `testReadTimeout`："Read timed out" 精确消息 + 超时后 socket 仍可读。
- `testByteStreamTransfer`：全双工字节流 round-trip（含 flush、部分读、available>0）。
- `testHalfClosePeerWrite`：半关闭后对端写仍成功（TCP 单向关闭语义）。
- `testSequentialConnections`、`testPostRefusalState`（refused 后 JDK-8 isClosed=false）、`testClosedAccessorDetail`、`testAcceptedStateDetail`（accept 侧 isConnected/isBound 与 init 状态）、`testSingleArgConnect`、`testTrafficClassBounds`（JDK-8 读回经 0xFC 掩码：255→252、1→0）、`testPortRanges`。

### JDK-8 vs JDK-27 差异（已按侧断言）

- 流级 write-after-close 消息："Socket closed"（访问器级为 "Socket is closed"）。
- refused connect 后 isClosed：JDK-8 false / JDK-27 true。
- trafficClass 读回：JDK-8 掩码 0xFC / JDK-27 精确回读。
