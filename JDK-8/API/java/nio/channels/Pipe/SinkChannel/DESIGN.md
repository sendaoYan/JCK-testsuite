# java.nio.channels.Pipe.SinkChannel 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。抽象结构（abstract + protected 构造器 + 继承面）、写契约
>（精确计数/FIFO/gather 顺序/非阻塞推进）、注册与关闭。

## 1. API 提供了哪些功能

- 抽象类 extends AbstractSelectableChannel，implements WritableByteChannel + GatheringByteChannel。
- write(ByteBuffer)/write(ByteBuffer[]) 聚集写；validOps()==OP_WRITE。

## 2. Java 用户如何使用

- 经 `Pipe.open().sink()` 获取；作为 WritableByteChannel 多态消费；非阻塞 + Selector 写事件。

## 3. 开源代码实际用法调研

- 作为 Pipe 一半被间接使用（Netty/Kafka 唤醒机制）；直接面向 SinkChannel 类型编程少见，
  但 gathering 写在序列化管线（多段 header+body 拼接）常见。

## 4. 测试用例设计 + 自动化实现 + 健壮性说明

文件：`SinkChannelTest.java`。

| 测试点 | 用例方法 |
|---|---|
| 抽象/protected 构造器/继承与接口面反射 | testWhiteBoxStructure |
| 精确写计数、多次写 FIFO 顺序、gather 顺序 | testWriteContracts |
| 阻塞模式 register → IllegalBlockingModeException；OP_READ 注册 → IAE；OP_WRITE 就绪 | testRegisterAndClose |
| 关闭后 write → ClosedChannelException；validOps 关闭后仍可查 | testRegisterAndClose |

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 负例经 `checkThrows` 断言**精确异常类**（`t.getClass() != ex` 即失败，子类不算）。
- 全部套接字用本地回环 127.0.0.1 + `bind(new InetSocketAddress(LO, 0))` 临时端口，不硬编码端口；
  辅助线程一律 daemon + `join(10_000)` 带超时；不使用 sleep 轮询（等待用 latch/select(超时)/parkNanos(300ms) 有界稳定）。
- 通道全部 try-with-resources / finally 关闭，无资源残留；headless 安全，不加 @key。

- JDK-8 兼容注：`Buffer.flip()` 在 JDK-8 返回 Buffer，赋值链需拆为语句。
- 本机实测断言数：35 条。
