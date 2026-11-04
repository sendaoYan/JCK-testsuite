# java.nio.channels.DatagramChannel 设计文档

## 1. API 功能

UDP 通道：无连接 send/receive、connect 配对读写、scatter/gather、disconnect。

（签名以 JDK-27 javap 实测为准。）

## 2. Java 用户如何使用

- 常规用法：直接面向 `DatagramChannel` 编程，或经 Files/Channels/Selector 等门面 API 间接触达。
- 组合用法：与本包/相关包其它类型配合（如视图+Files.getAttribute、通道+Selector、选项+Files.move）。
- 生命周期：打开/获取 -> 使用 -> 关闭/释放；AutoCloseable 资源用 try-with-resources。
- 失败路径：通过本类型或配套异常类型精确报告失败。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- DNS 解析器、NTP 客户端
- 用法特点：优先走 java.nio 高层门面（Files/Channels），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`DatagramChannelTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验 validOps；黑盒通过本地回环验证 sender 地址回读、connect 双向、disconnect 回到无连接、UNIX family 抛 UnsupportedOperationException。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除；套接字/异步用例仅用
  本地回环地址与临时端口（ServerSocketChannel.open().bind(null)），守护线程 + join/超时等待。
- 平台相关能力（posix/dos/user/acl 视图）以 `supportedFileAttributeViews()`/supportsFileAttributeView
  守护，不支持时打印 NOTE 并跳过行为部分，不判失败；headless 安全，不加 @key。
- 本机实测断言数：45 条。

### A6 契约深化（追加）

深化维度：
- send 未绑定先发送 → 自动绑定（通配地址 + 端口）；回环 send/receive 内容与发送方端口精确匹配。
- receive 截断语义：发 10 字节收 4 字节（余丢弃）；非阻塞无数据 → null。
- 未连接 read/write → NotYetConnectedException；connect(null) → NPE；connect 返回 this；
  disconnect 幂等；连接对字节流收发。
- **版本差异**：已连接通道 send 到其它地址 → JDK-8 IllegalArgumentException、
  JDK-27 AlreadyConnectedException，双侧各自断言并互注释。
- 选项矩阵（SO_BROADCAST 布尔、SO_KEEPALIVE → UOE）；关闭后 send/receive/read/write/connect →
  ClosedChannelException。

本机实测断言数：106 条（深化前 30）。
