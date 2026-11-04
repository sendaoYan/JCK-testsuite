# java.nio.channels.SocketChannel 设计文档

## 1. API 功能

TCP 客户端通道：阻塞/非阻塞 connect+finishConnect、读写、scatter/gather、选项与 socket() 适配器。

（签名以 JDK-27 javap 实测为准。）

## 2. Java 用户如何使用

- 常规用法：直接面向 `SocketChannel` 编程，或经 Files/Channels/Selector 等门面 API 间接触达。
- 组合用法：与本包/相关包其它类型配合（如视图+Files.getAttribute、通道+Selector、选项+Files.move）。
- 生命周期：打开/获取 -> 使用 -> 关闭/释放；AutoCloseable 资源用 try-with-resources。
- 失败路径：通过本类型或配套异常类型精确报告失败。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- Netty 传输层、Kafka/Redis 客户端、Cassandra
- 用法特点：优先走 java.nio 高层门面（Files/Channels），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`SocketChannelTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验接口层次与 validOps；黑盒通过本地回环 + 临时端口的守护线程 echo 服务器验证阻塞/非阻塞连接、ConnectionPendingException、gather 写/scatter 读、shutdownInput EOF、选项回读。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除；套接字/异步用例仅用
  本地回环地址与临时端口（ServerSocketChannel.open().bind(null)），守护线程 + join/超时等待。
- 平台相关能力（posix/dos/user/acl 视图）以 `supportedFileAttributeViews()`/supportsFileAttributeView
  守护，不支持时打印 NOTE 并跳过行为部分，不判失败；headless 安全，不加 @key。
- 本机实测断言数：53 条。

### A6 契约深化（追加）

深化维度：
- 状态机矩阵：未连接 read/write → NotYetConnectedException；无 pending 的 finishConnect →
  NoConnectionPendingException；已连接再 connect → AlreadyConnectedException；pending 期间再 connect →
  ConnectionPendingException；连接被拒 → java.net.ConnectException。
- 非阻塞 connect 三态流（pending ↔ !completedNow、OP_CONNECT 就绪、finishConnect 收尾）。
- 8 字节 + 64KiB 双向**字节精确**回环；非阻塞写推进（对端非阻塞排水 + 有界自旋 20 万次）。
- 对端关闭 → select 就绪 + read -1 粘性；shutdownInput → read -1。
- shutdownOutput 后 write：**版本差异**——JDK-8 抛精确 ClosedChannelException、JDK-27 抛精确
  AsynchronousCloseException，双侧各自断言并在代码互注释。
- socket() 适配器：同一性/端口/流回环；非阻塞模式流式读写 → IllegalBlockingModeException。
- Buffer 契约：write(null)/read(null) → NPE；read 入只读 → IAE；write 出只读视图合法。
- 关闭后 read/write/connect/finishConnect/bind/getOption → ClosedChannelException。

本机实测断言数：136 条（深化前 37）。
