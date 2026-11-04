# java.nio.channels.InterruptibleChannel 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。阻塞在通道操作上的线程被 interrupt/close 时的精确异常与通道状态。

## 1. API 提供了哪些功能

- 接口 extends Channel，唯一契约 close()：另一线程 interrupt 正阻塞在本通道操作上的线程
  → 该线程收到 ClosedByInterruptException、通道被关闭、**线程中断状态保留**；
  另一线程 close() → 阻塞线程收到 AsynchronousCloseException。

## 2. Java 用户如何使用

- 长阻塞网络操作可被 interrupt 取消（区别于传统 InputStream 不可中断 IO）；
  SocketChannel/ServerSocketChannel/DatagramChannel/Pipe 两端/FileChannel 均实现。

## 3. 开源代码实际用法调研

- Netty 关闭钩子、Kafka/OkHttp 的取消机制依赖中断关闭通道；行为对照 JDK 源码
  AbstractInterruptibleChannel.begin/end + sun.nio.ch.Interruptible。

## 4. 测试用例设计 + 自动化实现 + 健壮性说明

文件：`InterruptibleChannelTest.java`。

| 测试点 | 用例方法 |
|---|---|
| 接口结构（interface、extends Channel、close 方法面） | testInterruptibleChannel |
| 6 个实现类赋值矩阵（含 FileChannel）+ 经接口 close 生效 | testImplementors |
| 阻塞 read + interrupt → ClosedByInterruptException + 通道关闭 + 中断标志保留 | testInterruptBlockingRead |
| 阻塞 read + close → AsynchronousCloseException | testCloseWhileBlockingRead |
| 阻塞 accept + interrupt / close（同上两态） | testInterruptBlockingAccept/testCloseWhileBlockingAccept |
| 阻塞 receive + interrupt → 同语义 | testInterruptBlockingReceive |
| 阻塞 Pipe source read + interrupt → 同语义 | testInterruptPipeSourceRead |

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 负例经 `checkThrows` 断言**精确异常类**（`t.getClass() != ex` 即失败，子类不算）。
- 全部套接字用本地回环 127.0.0.1 + `bind(new InetSocketAddress(LO, 0))` 临时端口，不硬编码端口；
  辅助线程一律 daemon + `join(10_000)` 带超时；不使用 sleep 轮询（等待用 latch/select(超时)/parkNanos(300ms) 有界稳定）。
- 通道全部 try-with-resources / finally 关闭，无资源残留；headless 安全，不加 @key。

- 确定性：latch 确认线程启动 + parkNanos(300ms) 有界稳定后才 interrupt/close；join(10_000) 超时即失败
  （不挂死）；对端保持静默确保 read 真正阻塞。
- spi.AbstractInterruptibleChannel 属 A7 批次，本文件不触碰。
- 本机实测断言数：31 条。
