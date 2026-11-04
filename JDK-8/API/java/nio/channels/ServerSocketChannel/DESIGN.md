# java.nio.channels.ServerSocketChannel 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。open/bind/accept 状态机、未绑定/已绑定/已关闭矩阵、socket() 适配器、
选项契约与 Selector OP_ACCEPT 真实事件流。

## 1. API 提供了哪些功能

- **监听**：`open()`、`bind(SocketAddress,int backlog)`（null=通配地址）、`accept()`（未绑定 →
  NotYetBoundException；非阻塞无连接 → null）。
- **适配器**：`socket()` 返回 ServerSocket 桥接视图。
- **选项**：SO_RCVBUF/SO_REUSEADDR；SO_SNDBUF/IP_MULTICAST_TTL → UnsupportedOperationException。
- **结构**：validOps()==OP_ACCEPT；accepted 通道默认阻塞模式。

## 2. Java 用户如何使用

- 阻塞服务：bind → 循环 accept；非阻塞服务：configureBlocking(false) → register(OP_ACCEPT) → select 分发。
- backlog 参数控制 SYN 队列；bind(null) 绑定通配地址 + 临时端口。

## 3. 开源代码实际用法调研

- Netty NioServerSocketChannel、Tomcat NioEndpoint Acceptor、Kafka SocketServer：全部
  open→bind(0)→configureBlocking(false)→register(OP_ACCEPT) 流程。
- 坑：accept 前忘记 bind → NotYetBoundException；重复 bind → AlreadyBoundException。

## 4. 测试用例设计 + 自动化实现 + 健壮性说明

文件：`ServerSocketChannelTest.java`。

| 测试点 | 用例方法 |
|---|---|
| open 未绑定状态（localAddress null、socket().getLocalPort==-1） | testOpenState |
| 未绑定 accept → NotYetBoundException（阻塞/非阻塞两态） | testUnboundState |
| bind(null)/bind(回环) 语义、AlreadyBoundException、backlog 重载 | testBindSemantics |
| Selector OP_ACCEPT 事件流：selectNow 0 → 连接 → select==1 → accept 后 drained | testSelectorAcceptFlow |
| 阻塞 accept + 守护线程客户端 | testBlockingAcceptWithDaemonClient |
| socket() 适配器 accept（java.net.Socket 桥接） | testSocketAdaptor |
| 选项 set/get 往返 + 不支持选项 → UOE | testOptions/testOptionDetails |
| 关闭后 accept/bind/setOption/getOption → ClosedChannelException；close 幂等 | testCloseSemantics |
| 已绑定通道的注册与 validOps | testRegisterWhileBound |

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 负例经 `checkThrows` 断言**精确异常类**（`t.getClass() != ex` 即失败，子类不算）。
- 全部套接字用本地回环 127.0.0.1 + `bind(new InetSocketAddress(LO, 0))` 临时端口，不硬编码端口；
  辅助线程一律 daemon + `join(10_000)` 带超时；不使用 sleep 轮询（等待用 latch/select(超时)/parkNanos(300ms) 有界稳定）。
- 通道全部 try-with-resources / finally 关闭，无资源残留；headless 安全，不加 @key。

- 本机实测断言数：83 条。
