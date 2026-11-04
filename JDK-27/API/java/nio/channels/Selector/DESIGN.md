# java.nio.channels.Selector 设计文档

## 1. API 功能

多路复用器：select/selectNow/select(Consumer)、wakeup、keys/selectedKeys 生命周期。

（签名以 JDK-27 javap 实测为准。）

## 2. Java 用户如何使用

- 常规用法：直接面向 `Selector` 编程，或经 Files/Channels/Selector 等门面 API 间接触达。
- 组合用法：与本包/相关包其它类型配合（如视图+Files.getAttribute、通道+Selector、选项+Files.move）。
- 生命周期：打开/获取 -> 使用 -> 关闭/释放；AutoCloseable 资源用 try-with-resources。
- 失败路径：通过本类型或配套异常类型精确报告失败。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- Netty EventLoop、Kafka 网络层、ZooKeeper
- 用法特点：优先走 java.nio 高层门面（Files/Channels），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`SelectorTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验抽象性与 provider 绑定；黑盒验证 keys/selectedKeys 集合语义、守护线程 wakeup、真实 OP_ACCEPT 事件（select 后 readyOps）、select(Consumer) 变体、cancel 与 ClosedSelectorException。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除；套接字/异步用例仅用
  本地回环地址与临时端口（ServerSocketChannel.open().bind(null)），守护线程 + join/超时等待。
- 平台相关能力（posix/dos/user/acl 视图）以 `supportedFileAttributeViews()`/supportsFileAttributeView
  守护，不支持时打印 NOTE 并跳过行为部分，不判失败；headless 安全，不加 @key。
- 本机实测断言数：37 条。

### A6 契约深化（追加）

深化维度：
- select(负超时) → IllegalArgumentException（精确类）；正超时上下界测量。
- wakeup 粘性：预触发后 select(60_000)/select(0) 立即返回 0（消除时序竞态）；双重 wakeup 不叠加。
- 守护线程 wakeup 唤醒阻塞 select（latch + join(10_000) 有界）；close() 唤醒阻塞 select 返回 0。
- closed selector 后 keys/selectedKeys/selectNow/select(long)/select → ClosedSelectorException 矩阵；
  wakeup() 关闭后 no-op 返回 this。
- selectedKeys 协议：add → UnsupportedOperationException，remove/clear 允许。
- 一个通道注册两个 selector：**连接后的回环对**上断言 not-ready（未连接 socket 对 READ/WRITE
  都可能立即就绪，这是本批实测出的平台事实）。
- JDK-11+ 专属：selectNow(Consumer) 返回 0 且消费者不 invoked（JDK-27 侧断言）。

本机实测断言数：111 条（深化前 31）。
