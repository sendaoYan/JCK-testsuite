# java.nio.channels.Selector 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。select/selectNow/wakeup/keys/selectedKeys 的语义矩阵、粘性 wakeup、
> select(0)/负超时、ClosedSelectorException 矩阵与多选择器共享通道。

## 1. API 提供了哪些功能

- **选择**：`select()`、`select(long timeoutMs)`、`selectNow()`；负超时 → IllegalArgumentException。
- **唤醒**：`wakeup()`（粘性：预触发使下一次 select 立即返回 0；关闭后调用 no-op 返回 this）。
- **集合**：`keys()`（注册键全集）、`selectedKeys()`（就绪子集，仅 remove/clear 允许，add → UOE）。
- **生命周期**：`open()`/`close()`（幂等）；关闭后各操作 → ClosedSelectorException。

## 2. Java 用户如何使用

- 标准事件循环：`int n = selector.select(1000); Iterator it = selector.selectedKeys().iterator(); ...`
- wakeup() 供其它线程打破阻塞的 select（注册新通道前唤醒）。
- select(0) 阻塞至事件；selectNow 永不阻塞。

## 3. 开源代码实际用法调研

- Netty NioEventLoop.select（wakeup 机制做任务队列通知）、Kafka Selector.poll、ZooKeeper、Redis/Jedis 多路复用。
- 常见坑：selectedKeys 必须 remove；未连接 socket 对 OP_READ/OP_WRITE 都可能立即就绪，做“未就绪”
  断言必须先建立连接。

## 4. 测试用例设计 + 自动化实现 + 健壮性说明

文件：`SelectorTest.java`。

| 测试点 | 用例方法 |
|---|---|
| open/close 幂等、isOpen、provider 同源 | testLifecycleAndProvider |
| keys/selectedKeys 只读协议：add→UOE、remove/clear 允许 | testSetSemantics |
| select(负) → IAE 精确；正超时上下界测量 | testSelectTimeouts |
| wakeup 粘性（预触发 → select(60_000) 立即 0） | testWakeupStickiness |
| 守护线程 wakeup 唤醒阻塞 select（latch + join） | testWakeupFromDaemonThread |
| select(0) 阻塞语义（wakeup 打断返回 0） | testSelectZeroBlockingSemantics |
| closed selector 上 register → ClosedSelectorException；null selector → NPE | testRegisterLifecycle |
| selectedKeys 增删流程与清空 | testSelectedKeysFlow |
| cancel 后 keys 收缩、selectNow 生效 | testCancelSemantics |
| 关闭后 5 操作异常矩阵 + wakeup no-op | testClosedSelectorOperations |
| 一个通道注册两个 selector（连接回环对上 not-ready 断言） | testTwoSelectorsOneChannel |
| close() 唤醒阻塞中的 select 返回 0 | testCloseWakesBlockedSelect |

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 负例经 `checkThrows` 断言**精确异常类**（`t.getClass() != ex` 即失败，子类不算）。
- 全部套接字用本地回环 127.0.0.1 + `bind(new InetSocketAddress(LO, 0))` 临时端口，不硬编码端口；
  辅助线程一律 daemon + `join(10_000)` 带超时；不使用 sleep 轮询（等待用 latch/select(超时)/parkNanos(300ms) 有界稳定）。
- 通道全部 try-with-resources / finally 关闭，无资源残留；headless 安全，不加 @key。

- 粘性 wakeup 消除 wakeup/select 的时序竞态；阻塞线程场景 latch 确认启动 + parkNanos(300ms) 有界稳定。
- 本机实测断言数：75 条。
