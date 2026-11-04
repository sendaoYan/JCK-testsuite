# java.nio.channels.NetworkChannel 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。bind/getLocalAddress/setOption/getOption/supportedOptions
> 经三种通道（Socket/ServerSocket/Datagram）的接口级一致性、未绑定地址、关闭后矩阵。

## 1. API 提供了哪些功能

- 接口：`bind(SocketAddress)`（null=通配；返回 this）、`getLocalAddress()`（未绑定 null；
  已关闭 → ClosedChannelException，javadoc 明确）、`setOption`（返回 this 链式）、
  `getOption`、`supportedOptions()`。

## 2. Java 用户如何使用

- 通过接口类型多态操作三种网络通道；选项链式 `ch.setOption(A,..).setOption(B,..)`。

## 3. 开源代码实际用法调研

- Netty ChannelOption 体系、Kafka/OkHttp 在通道上设置 SO_KEEPALIVE/SO_SNDBUF；
  接口直接消费多见于通用网络库抽象层。

## 4. 测试用例设计 + 自动化实现 + 健壮性说明

文件：`NetworkChannelTest.java`。

| 测试点 | 用例方法 |
|---|---|
| 未绑定/未连接时 getLocalAddress/getRemoteAddress == null | testUnboundAndUnconnectedAddresses |
| bind(null) 通配 + 临时端口、bind(回环) 精确、AlreadyBoundException | testUnboundAndUnconnectedAddresses |
| 选项链式/往返（经接口类型） | testOptions/testContractThroughDatagramChannel |
| 不支持选项 set/get → UnsupportedOperationException | testOptionDetails |
| 关闭后 bind/getOption/setOption → ClosedChannelException、getLocalAddress → CCE（javadoc） | testClosedChannelRules |
| supportedOptions 关闭后仍可查询 | testClosedChannelRules |

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 负例经 `checkThrows` 断言**精确异常类**（`t.getClass() != ex` 即失败，子类不算）。
- 全部套接字用本地回环 127.0.0.1 + `bind(new InetSocketAddress(LO, 0))` 临时端口，不硬编码端口；
  辅助线程一律 daemon + `join(10_000)` 带超时；不使用 sleep 轮询（等待用 latch/select(超时)/parkNanos(300ms) 有界稳定）。
- 通道全部 try-with-resources / finally 关闭，无资源残留；headless 安全，不加 @key。

- 本机实测断言数：43 条。
