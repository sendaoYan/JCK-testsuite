# java.nio.channels.SocketChannel 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。连接状态机（NotYetConnected/AlreadyConnected/ConnectionPending/
> NoConnectionPending）、非阻塞 connect 流程、精确字节读写、shutdownInput/Output、
> socket() 桥接、选项与 Buffer 契约。

## 1. API 提供了哪些功能

- **连接**：`open()`/`open(addr)`（阻塞直连）、`connect(addr)`（非阻塞返回 false 立即返回）、
  `finishConnect()`、`isConnected()/isConnectionPending()`、`getRemoteAddress()/getLocalAddress()`。
- **读写**：read/write 单缓冲 + gathering/scattering；非阻塞 0 返回；EOF -1。
- **半关闭**：`shutdownInput()`（之后 read → -1）、`shutdownOutput()`（之后 write → 异常）。
- **适配器**：`socket()`；非阻塞模式下流式读写 → IllegalBlockingModeException。

## 2. Java 用户如何使用

- 非阻塞客户端：connect 返回 false → register(OP_CONNECT) → finishConnect 后读写。
- 回环测试对：ServerSocketChannel bind(0) + open(addr) + accept 构成确定性数据通道。

## 3. 开源代码实际用法调研

- Netty NioSocketChannel（unsafe.connect/finishConnect 状态机）、Kafka InFlightChannel、
  Elasticsearch/OkHttp 均封装 connect 三态。
- 高频坑：ConnectionPending 期间再次 connect；未连接 read/write → NotYetConnectedException。

## 4. 测试用例设计 + 自动化实现 + 健壮性说明

文件：`SocketChannelTest.java`。

| 测试点 | 用例方法 |
|---|---|
| open 初始状态（未绑定/未连接/阻塞） | testOpenState |
| bind(null)/回环绑定、AlreadyBound | testOpenAndBindState/testBindSemantics |
| 未连接 read/write → NotYetConnected、finishConnect → NoConnectionPending | testUnconnectedOperations |
| 阻塞连接流（返回值、远程地址）；已连接再 connect → AlreadyConnected | testBlockingConnectFlow/testConnectFlows |
| 非阻塞 connect：pending↔!completed、ConnectionPending、OP_CONNECT | testNonBlockingConnectFlow |
| 8 字节 + 64KiB 双向精确字节回环 | testReadWriteExact |
| 聚集/分散写读顺序（JDK-8 gather/scatter） | testGatheringScattering |
| 非阻塞写推进（对端非阻塞排水 + 有界自旋） | testNonBlockingWriteProgress |
| 对端关闭 → select 就绪 + read -1 粘性 | testReadEofOnPeerClose |
| shutdownOutput 后 write → JDK-8 精确 ClosedChannelException（JDK-27 为 AsynchronousCloseException，双侧各自断言） | testShutdownOutput |
| shutdownInput → read -1 | testShutdownInput |
| 连接被拒 → java.net.ConnectException | testConnectRefused |
| 关闭后 read/write/connect/finishConnect/bind → ClosedChannelException | testCloseSemantics |
| socket() 适配器：同一性/端口/流回环/非阻塞流 → IllegalBlockingModeException | testSocketAdaptorBridge |
| 选项矩阵 | testOptions/testOptionDetails |
| Buffer 契约：write(null)/read(null) → NPE；read 入只读 → IAE；write 出只读合法 | testBufferContracts |

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 负例经 `checkThrows` 断言**精确异常类**（`t.getClass() != ex` 即失败，子类不算）。
- 全部套接字用本地回环 127.0.0.1 + `bind(new InetSocketAddress(LO, 0))` 临时端口，不硬编码端口；
  辅助线程一律 daemon + `join(10_000)` 带超时；不使用 sleep 轮询（等待用 latch/select(超时)/parkNanos(300ms) 有界稳定）。
- 通道全部 try-with-resources / finally 关闭，无资源残留；headless 安全，不加 @key。

- 本机实测断言数：131 条。
