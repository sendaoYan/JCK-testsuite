# java.nio.channels.SelectionKey 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。OP_* 常量、interestOps 校验、attach 生命周期、isValid/cancel 语义、
> CancelledKeyException 矩阵，以及真实回环事件流上的就绪谓词断言。

## 1. API 提供了哪些功能

- **常量**：OP_READ=1、OP_WRITE=4、OP_CONNECT=8、OP_ACCEPT=16（单比特、互斥、并集 29）。
- **兴趣/就绪集**：`interestOps(int)`（返回 this，校验合法位）、`interestOps()`、`readyOps()`、
  `isReadable()/isWritable()/isConnectable()/isAcceptable()`。
- **生命周期**：`isValid()`、`cancel()`；attachment：`attach(Object)`/`attachment()`。
- **关联**：`channel()`、`selector()`。

## 2. Java 用户如何使用

- 事件循环里用 `readyOps() & OP_READ` 分发；处理完 `iterator.remove()`。
- attachment 携带会话对象；重新 register 同一 selector 返回同一 key 并更新 interestOps。
- 已取消 key 的谓词全部抛 CancelledKeyException，但 attach/channel/selector 仍可读。

## 3. 开源代码实际用法调研

- Netty NioEventLoop.processSelectedKey 按 readyOps 位分发读写事件；Kafka KSelector 用 attachment 保存节点状态；
  Tomcat NioEndpoint 用 interestOps 增减控制事件订阅。
- 共识：selectedKeys 迭代中必须 remove，否则重复处理。

## 4. 测试用例设计 + 自动化实现 + 健壮性说明

文件：`SelectionKeyTest.java`。

| 测试点 | 用例方法 |
|---|---|
| 常量值/单比特/并集 29 矩阵 | testConstantMatrix |
| interestOps 往返、0 合法、非法位(1<<5)/OP_ACCEPT/负数 → IAE | testInterestOpsContract |
| register 附带 attachment、再注册更新 ops 且清 attachment（RI 语义） | testRegisterWithAttachment |
| attach 置换/置 null 清除 | testAttachSemantics |
| isValid：通道关闭/选择器关闭/cancel 后变 false | testIsValidLifecycle |
| 取消后 7 个谓词 → CancelledKeyException；attach/channel/selector 仍可用 | testCancelledKeyExceptions |
| 真实 accept/read/write 事件流的 readyOps 精确值 | testReadinessPredicates |
| OP_CONNECT 非阻塞连接完成 → readyOps==8 | testConnectReadiness |

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 负例经 `checkThrows` 断言**精确异常类**（`t.getClass() != ex` 即失败，子类不算）。
- 全部套接字用本地回环 127.0.0.1 + `bind(new InetSocketAddress(LO, 0))` 临时端口，不硬编码端口；
  辅助线程一律 daemon + `join(10_000)` 带超时；不使用 sleep 轮询（等待用 latch/select(超时)/parkNanos(300ms) 有界稳定）。
- 通道全部 try-with-resources / finally 关闭，无资源残留；headless 安全，不加 @key。

- 版本差异（双侧各自断言）：interestOps 切到 OP_WRITE 后 JDK-8 readyOps 精确等于 OP_WRITE（4）；
  JDK-27 保留先前选中的 OP_READ 位（5），该侧只断言包含 OP_WRITE。
- 本机实测断言数：77 条。
