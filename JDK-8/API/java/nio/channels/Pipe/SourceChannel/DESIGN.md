# java.nio.channels.Pipe.SourceChannel 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。抽象结构、读契约（精确内容/scatter 顺序/非阻塞 0）、
> sink 关闭后 EOF -1、注册与关闭。

## 1. API 提供了哪些功能

- 抽象类 extends AbstractSelectableChannel，implements ReadableByteChannel + ScatteringByteChannel。
- read(ByteBuffer)/read(ByteBuffer[]) 分散读；validOps()==OP_READ。

## 2. Java 用户如何使用

- 经 `Pipe.open().source()` 获取；非阻塞 + Selector 读事件（唤醒机制读取侧）。

## 3. 开源代码实际用法调研

- 事件循环被唤醒后读 1 字节丢弃；scattering 读在网络解析（定长头 + 变长体）常见。

## 4. 测试用例设计 + 自动化实现 + 健壮性说明

文件：`SourceChannelTest.java`。

| 测试点 | 用例方法 |
|---|---|
| 抽象/protected 构造器/继承与接口面反射 | testWhiteBoxStructure |
| 精确内容、scatter 跨缓冲顺序 | testReadContracts |
| sink 关闭 → 排空后 read -1 粘性 | testEofAfterSinkClose |
| 阻塞模式 register → IllegalBlockingModeException；OP_WRITE 注册 → IAE；OP_READ 就绪 | testRegisterAndClose |
| 关闭后 read → ClosedChannelException | testRegisterAndClose |

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 负例经 `checkThrows` 断言**精确异常类**（`t.getClass() != ex` 即失败，子类不算）。
- 全部套接字用本地回环 127.0.0.1 + `bind(new InetSocketAddress(LO, 0))` 临时端口，不硬编码端口；
  辅助线程一律 daemon + `join(10_000)` 带超时；不使用 sleep 轮询（等待用 latch/select(超时)/parkNanos(300ms) 有界稳定）。
- 通道全部 try-with-resources / finally 关闭，无资源残留；headless 安全，不加 @key。

- 本机实测断言数：35 条。
