# java.nio.channels.Pipe 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。sink/source 两端的同一性与生命周期、FIFO 精确字节流、
> 非阻塞语义、Selector 集成、EOF 与 Broken pipe 半关闭。

## 1. API 提供了哪些功能

- `Pipe.open()`；`sink()`/`source()`（同一 Pipe 上多次调用返回同一对象）。
- sink：WritableByteChannel + GatheringByteChannel（validOps==OP_WRITE）；
  source：ReadableByteChannel + ScatteringByteChannel（validOps==OP_READ）。
- 语义：单向 FIFO；source 关闭后 sink 写 → IOException（Broken pipe）；
  sink 关闭后 source 排空后 read → -1。

## 2. Java 用户如何使用

- 线程间字节通道（唤醒 Selector 事件循环的标准姿势：写一个字节 + OP_READ）。
- Reactor 模式中主线程向 IO 线程投递任务。

## 3. 开源代码实际用法调研

- Netty NioEventLoop 的唤醒机制（wakeupTaskInQueue + pipe/socket 自管道）、Kafka wakeup、
  ZooKeeper ClientCnxnSocketNIO。
- 坑：无人读取时 pipe 缓冲（默认 64KB）写满后阻塞；对端关闭写侧收到 Broken pipe IOException。

## 4. 测试用例设计 + 自动化实现 + 健壮性说明

文件：`PipeTest.java`。

| 测试点 | 用例方法 |
|---|---|
| sink()/source() 同一性、validOps、blocking 翻转 | testEndsBasics/testEndsIdentity |
| 16 字节 FIFO 精确 + gather/scatter 顺序 | testFifoRoundTripExact |
| 非阻塞：source read 0、sink 小写完成 | testNonBlockingSemantics |
| 写入后 OP_READ 就绪；sink 立即可写 | testSelectorIntegration/testSinkWritableReadiness |
| sink 关闭 → 排空后 -1 粘性 | testSinkCloseEof |
| source 关闭 → 有界写循环触发精确 java.io.IOException（Broken pipe） | testSourceCloseBrokenPipe |
| 两端各自关闭后操作 → ClosedChannelException；重复关闭幂等 | testClosedEndOperations |
| 抽象结构反射白盒 | testWhiteBoxStructure |

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 负例经 `checkThrows` 断言**精确异常类**（`t.getClass() != ex` 即失败，子类不算）。
- 全部套接字用本地回环 127.0.0.1 + `bind(new InetSocketAddress(LO, 0))` 临时端口，不硬编码端口；
  辅助线程一律 daemon + `join(10_000)` 带超时；不使用 sleep 轮询（等待用 latch/select(超时)/parkNanos(300ms) 有界稳定）。
- 通道全部 try-with-resources / finally 关闭，无资源残留；headless 安全，不加 @key。

- Broken pipe 用 4096 次 64KiB 有界写触发（不依赖内核缓冲大小的确定性上限）。
- 本机实测断言数：56 条。
