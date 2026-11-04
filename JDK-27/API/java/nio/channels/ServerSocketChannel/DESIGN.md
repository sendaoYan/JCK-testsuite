# java.nio.channels.ServerSocketChannel 设计文档

## 1. API 功能

TCP 服务端通道：bind/accept、阻塞与非阻塞接受、validOps=OP_ACCEPT。

（签名以 JDK-27 javap 实测为准。）

## 2. Java 用户如何使用

- 常规用法：直接面向 `ServerSocketChannel` 编程，或经 Files/Channels/Selector 等门面 API 间接触达。
- 组合用法：与本包/相关包其它类型配合（如视图+Files.getAttribute、通道+Selector、选项+Files.move）。
- 生命周期：打开/获取 -> 使用 -> 关闭/释放；AutoCloseable 资源用 try-with-resources。
- 失败路径：通过本类型或配套异常类型精确报告失败。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- Netty boss 线程、Tomcat NIO connector、Kafka broker
- 用法特点：优先走 java.nio 高层门面（Files/Channels），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`ServerSocketChannelTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验方法面；黑盒验证未 bind 时 accept 抛 NotYetBoundException、重复 bind 抛 AlreadyBoundException、阻塞 accept 收到真实连接、非阻塞 accept 返回 null。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除；套接字/异步用例仅用
  本地回环地址与临时端口（ServerSocketChannel.open().bind(null)），守护线程 + join/超时等待。
- 平台相关能力（posix/dos/user/acl 视图）以 `supportedFileAttributeViews()`/supportsFileAttributeView
  守护，不支持时打印 NOTE 并跳过行为部分，不判失败；headless 安全，不加 @key。
- 本机实测断言数：30 条。

### A6 契约深化（追加）

深化维度：
- 未绑定 accept → NotYetBoundException（阻塞/非阻塞两态）；未绑定 socket().getLocalPort==-1。
- bind(null) 通配地址 + 临时端口、bind(回环) 精确、重复 bind → AlreadyBoundException、backlog 重载。
- Selector OP_ACCEPT 完整事件流：selectNow 0 → 客户端连接 → select==1 → readyOps==OP_ACCEPT →
  accept → selectedKeys.remove → drained。
- 阻塞 accept + 守护线程客户端；socket() 适配器 accept（java.net 桥接）。
- 选项：SO_RCVBUF>=、SO_REUSEADDR 布尔往返；SO_SNDBUF/IP_MULTICAST_TTL → UnsupportedOperationException。
- 关闭后 accept/bind/setOption/getOption → ClosedChannelException；close 幂等。

本机实测断言数：80 条（深化前 25）。
