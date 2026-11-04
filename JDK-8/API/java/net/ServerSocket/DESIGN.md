# java.net.ServerSocket 测试设计案（JDK-8）

> 批次 F 深化：unbound→bound→closed 完整状态机、回环 accept、accept 超时精确消息、选项矩阵、一次性 socket factory。全程回环（`ServerSocket(0,50,127.0.0.1)`），daemon 线程，finally 关闭。

## 1. API 功能

- 构造器：`()`、`(int)`、`(int,int)`、`(int,int,InetAddress)`；未绑定构造 + `bind(SocketAddress,int)` 两段式。
- 状态：`isBound/isClosed`、`getLocalPort/getLocalSocketAddress/InetAddress`。
- 连接：`accept()`、`setSoTimeout/getSoTimeout`、`close()`。
- 选项：`setReceiveBufferSize`、`setReuseAddress`（bind 前语义）。
- 工厂：`setSocketFactory(SocketImplFactory)`（JVM 全局一次性）。

## 2. 用户如何使用

`new ServerSocket(0, 50, 127.0.0.1)` 绑定回环临时端口，`accept()` 阻塞取连接；`setSoTimeout` 让 accept 有界超时；`close()` 停止监听。

## 3. 测试用例设计（ServerSocketTest.java，151 断言）

- `testUnboundState`：未绑定访问器、accept/bind 前置异常、toString。
- `testBindLifecycle`：双 bind→BindException（"Address already in use (Bind failed)"）、bind(null)、重复 bind。
- `testAcceptTimeoutTyping`：SoTimeout(200) 后 accept→SocketTimeoutException "Accept timed out"，超时后 server 仍可用；SoTimeout 存活于超时。
- 回环 accept 对：daemon acceptor + `ArrayBlockingQueue` 同步，accept 返回 socket 已连接已绑定。
- `testClosedSemantics`：close 后 accept→"Socket is closed"、getLocalPort 保留、close 幂等。
- 选项矩阵：负 SoTimeout→IAE、负 RCVBUF→IAE、正/零边界。
- factory（main 末尾）：`setSocketFactory` 二次→SocketException "factory already defined"。

## 易错规范细节（已锁入断言）

- 未绑定时 `accept()`→SocketException "Not bound"；`getLocalPort()`=-1。
- 已关闭后 `accept()`→SocketException "Socket is closed"；`isBound()` 保持 true。
- `setSoTimeout(负)`→IllegalArgumentException；0 表示无限期阻塞。
- 端口互易：accept 侧 `getLocalPort()` == server `getLocalPort()`。

## JDK-8 vs JDK-27 差异

- bind 失败消息：JDK-8 "Address already in use (Bind failed)" / JDK-27 "Address already in use"。
- JDK-27 侧另有 `getOption/setOption(SocketOption)`（JDK 9+）矩阵，共 167 断言。
