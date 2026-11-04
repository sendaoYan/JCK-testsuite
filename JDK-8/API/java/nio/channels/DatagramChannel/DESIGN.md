# java.nio.channels.DatagramChannel 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。send 自动绑定、receive 截断、connect/disconnect 状态机、
> socket() 桥接、选项矩阵与关闭后异常。

## 1. API 提供了哪些功能

- **收发**：`send(ByteBuffer,SocketAddress)`（未绑定自动绑通配地址）、`receive(ByteBuffer)`
  （返回发送方地址；缓冲不足截断丢弃剩余）、connect 后字节流式 `read/write`。
- **连接**：`connect(SocketAddress)`（返回 this，仅过滤作用）、`disconnect()`（幂等）、
  `getRemoteAddress()`。
- **适配器**：`socket()`；**选项**：SO_RCVBUF/SO_SNDBUF/SO_REUSEADDR/SO_BROADCAST 等；
  SO_KEEPALIVE → UnsupportedOperationException。

## 2. Java 用户如何使用

- 无连接 Echo：open → bind(0) → receive/send；连接式过滤对端：connect 后 read/write。
- receive 返回 null 表示非阻塞无数据。

## 3. 开源代码实际用法调研

- Netty NioDatagramChannel、KVell/cassandra UDP 路径、DNS 客户端（dnsjava）。
- 坑：receive 缓冲太小静默截断；已连接 send 到其它地址被拒（JDK-8 IllegalArgumentException /
  JDK-27 AlreadyConnectedException，双侧各自断言）。

## 4. 测试用例设计 + 自动化实现 + 健壮性说明

文件：`DatagramChannelTest.java`。

| 测试点 | 用例方法 |
|---|---|
| open 初始状态 | testOpenState |
| send 前未绑定 → 自动绑定（通配地址 + 端口） | testSendAutoBinds |
| 回环 send/receive 内容与发送方端口精确匹配 | testLoopbackSendReceive |
| 非阻塞 receive 无数据 → null | testNonBlockingReceiveNull |
| receive 截断：发 10 收 4 | testReceiveTruncation |
| 未连接 read/write → NotYetConnectedException | testUnconnectedReadWrite |
| connect(null) → NPE、返回 this、disconnect 幂等、连接式字节流 | testConnectSemantics |
| 已连接 send 到其它地址 → 精确异常（版本差异） | testConnectSemantics |
| socket() 桥接 | testSocketBridge |
| 选项矩阵 + 不支持选项 UOE | testOptions/testOptionDetails |
| 关闭后 send/receive/read/write/connect → ClosedChannelException | testCloseSemantics |

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 负例经 `checkThrows` 断言**精确异常类**（`t.getClass() != ex` 即失败，子类不算）。
- 全部套接字用本地回环 127.0.0.1 + `bind(new InetSocketAddress(LO, 0))` 临时端口，不硬编码端口；
  辅助线程一律 daemon + `join(10_000)` 带超时；不使用 sleep 轮询（等待用 latch/select(超时)/parkNanos(300ms) 有界稳定）。
- 通道全部 try-with-resources / finally 关闭，无资源残留；headless 安全，不加 @key。

- 本机实测断言数：97 条。
