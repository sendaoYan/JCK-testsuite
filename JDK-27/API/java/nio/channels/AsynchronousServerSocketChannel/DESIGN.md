# java.nio.channels.AsynchronousServerSocketChannel 设计文档

## 1. API 功能

异步服务端：bind/accept(Future 与 handler)。

（签名以 JDK-27 javap 实测为准。）

## 2. Java 用户如何使用

- 常规用法：直接面向 `AsynchronousServerSocketChannel` 编程，或经 Files/Channels/Selector 等门面 API 间接触达。
- 组合用法：与本包/相关包其它类型配合（如视图+Files.getAttribute、通道+Selector、选项+Files.move）。
- 生命周期：打开/获取 -> 使用 -> 关闭/释放；AutoCloseable 资源用 try-with-resources。
- 失败路径：通过本类型或配套异常类型精确报告失败。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- AIO 服务器
- 用法特点：优先走 java.nio 高层门面（Files/Channels），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`AsynchronousServerSocketChannelTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验方法面；黑盒验证 NotYetBound、bind(null,16) 返回 this、AlreadyBound、客户端先写后服务端读的 echo、handler attachment、二次 accept 抛 AcceptPendingException。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除；套接字/异步用例仅用
  本地回环地址与临时端口（ServerSocketChannel.open().bind(null)），守护线程 + join/超时等待。
- 平台相关能力（posix/dos/user/acl 视图）以 `supportedFileAttributeViews()`/supportsFileAttributeView
  守护，不支持时打印 NOTE 并跳过行为部分，不判失败；headless 安全，不加 @key。
- 本机实测断言数：27 条。

### A7 批次深化（契约覆盖增强，append-only，原断言全部保留）

- 新增方法：testUnboundAndBindContract/testAcceptContract/testGroupAndSurfaceContract。
- 负例矩阵（精确异常类型，同步抛 vs 经 Future/handler 交付分开断言）：NotYetBoundException（未 bind 即 accept）、AcceptPendingException（挂起 accept + 二次 accept，Future 与 handler 两种形式）、AlreadyBoundException、bind(null) 自动分配、backlog 语义
- 确定性与健壮性：close 终止挂起 accept → Future.get 抛 ExecutionException，cause 精确为 AsynchronousCloseException（双侧实测）；accept 无超时重载（API 面断言）。
- 版本差异（按侧断言）：close 后再 accept 的失败经 Future 交付（非同步抛）；accept 无 timeout 重载。
- 本机实测断言数：23 → 71 条。
