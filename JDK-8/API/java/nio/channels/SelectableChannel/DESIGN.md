# java.nio.channels.SelectableChannel 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。validOps/register/keyFor/configureBlocking/blockingLock 的语义与
> 阻塞模式互斥规则，配合 Selector 的注册生命周期与负例精确异常。

## 1. API 提供了哪些功能

- **多路复用注册**：`register(Selector,int)`/`register(Selector,int,Object)`（返回 SelectionKey）、
  `keyFor(Selector)`、`validOps()`（每子类一个固定位集）。
- **阻塞模式**：`configureBlocking(boolean)`（返回 this）、`isBlocking()`（默认 true）、
  `blockingLock()`（模式切换的同步锁对象）。
- **结构**：抽象类，extends AbstractSelectableChannel；`provider()` 绑定创建它的 SelectorProvider。
- **互斥规则**：阻塞模式下 register → IllegalBlockingModeException；关闭后 register/configureBlocking
  → ClosedChannelException。

## 2. Java 用户如何使用

- 网络编程标准三步：`channel.configureBlocking(false)` → `channel.register(selector, ops)` → 事件循环处理。
- `blockingLock()` 用于与其它线程协调模式切换（加锁后 configureBlocking 原子）。
- 每类通道合法兴趣集固定：ServerSocketChannel==OP_ACCEPT、SocketChannel==READ|WRITE|CONNECT、
  DatagramChannel==READ|WRITE、Pipe 两端各一。

## 3. 开源代码实际用法调研

- Netty（AbstractNioChannel 构造时 configureBlocking(false)）、Kafka NetworkClient、ZooKeeper NIOServerCnxn：
  注册前必须先切非阻塞是高频坑（IllegalBlockingModeException）。
- 用法特点：register + interestOps 调整驱动事件循环；blockingLock 极少被应用层直接使用。

## 4. 测试用例设计 + 自动化实现 + 健壮性说明

文件：`SelectableChannelTest.java`（check/checkThrows 断言体系，main 顺序调用各 test 方法）。

| 测试点 | 用例方法 |
|---|---|
| 四类通道 validOps 精确矩阵 | testValidOpsMatrix |
| 阻塞模式 register → IllegalBlockingModeException（每类通道） | testRegisterWhileBlocking |
| configureBlocking 返回 this / 状态翻转 | testConfigureBlockingReturnsThis |
| blockingLock 同一性 + 同线程持锁可重入 | testBlockingLockContract |
| 非法兴趣位 / 负数 → IllegalArgumentException；ops=0 合法 | testRegisterInvalidOps |
| 关闭后 register/configureBlocking → ClosedChannelException | testClosedChannelRules |
| provider 与 SelectorProvider.provider() 同源 | testProviderContract |
| keyFor 未注册 null / 注册后同一 key / 关闭后 null | testKeyForSemantics |
| 阻塞中的通道被并发 register 的拒绝 | testRegisteredBlockingConflict |
| 抽象性 / 抽象方法面反射白盒 | testWhiteBoxStructure |

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 负例经 `checkThrows` 断言**精确异常类**（`t.getClass() != ex` 即失败，子类不算）。
- 全部套接字用本地回环 127.0.0.1 + `bind(new InetSocketAddress(LO, 0))` 临时端口，不硬编码端口；
  辅助线程一律 daemon + `join(10_000)` 带超时；不使用 sleep 轮询（等待用 latch/select(超时)/parkNanos(300ms) 有界稳定）。
- 通道全部 try-with-resources / finally 关闭，无资源残留；headless 安全，不加 @key。

- 本机实测断言数：75 条。
