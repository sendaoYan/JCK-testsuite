# java.nio.channels.AsynchronousSocketChannel 设计文档

## 1. API 功能

异步客户端：connect/read/write 超时与 pending 状态机。

（签名以 JDK-27 javap 实测为准。）

## 2. Java 用户如何使用

- 常规用法：直接面向 `AsynchronousSocketChannel` 编程，或经 Files/Channels/Selector 等门面 API 间接触达。
- 组合用法：与本包/相关包其它类型配合（如视图+Files.getAttribute、通道+Selector、选项+Files.move）。
- 生命周期：打开/获取 -> 使用 -> 关闭/释放；AutoCloseable 资源用 try-with-resources。
- 失败路径：通过本类型或配套异常类型精确报告失败。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- AIO 客户端
- 用法特点：优先走 java.nio 高层门面（Files/Channels），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`AsynchronousSocketChannelTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验方法面；黑盒验证 handler connect、定时读写、scatter/gather、读超时抛 InterruptedByTimeoutException、shutdownOutput 对端 EOF、ReadPendingException、未连接 NotYetConnectedException。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除；套接字/异步用例仅用
  本地回环地址与临时端口（ServerSocketChannel.open().bind(null)），守护线程 + join/超时等待。
- 平台相关能力（posix/dos/user/acl 视图）以 `supportedFileAttributeViews()`/supportsFileAttributeView
  守护，不支持时打印 NOTE 并跳过行为部分，不判失败；headless 安全，不加 @key。
- 本机实测断言数：33 条。

### A7 批次深化（契约覆盖增强，append-only，原断言全部保留）

- 新增方法：testConnectStateContract/testReadWriteContract/testPendingAndCloseContract/testSurfaceContract/testChannelBasicsContract。
- 负例矩阵（精确异常类型，同步抛 vs 经 Future/handler 交付分开断言）：ConnectionPendingException（双 connect 同步抛）、NotYetConnectedException、AlreadyConnectedException、ReadPendingException（挂起读+二次读）、WritePendingException（分块写饱和至 !isDone 后二次写）、IllegalArgumentException（只读缓冲/负 position）、shutdownOutput 后对端 read 返回 -1（EOF）
- 确定性与健壮性：确定性构造：回环地址+临时端口；无数据+长超时构造挂起读；256KB 分块写至 future 未完成构造挂起写；挂起操作 + close → AsynchronousCloseException（handler failed 与 Future ExecutionException.getCause 双通道）。
- 版本差异（按侧断言）：异步 close 的失败经 Future（ExecutionException cause=ClosedChannelException/AsynchronousCloseException）与 handler failed() 交付，同步方法不抛；写大缓冲区可部分完成后挂起（非一次挂起）。
- 本机实测断言数：24 → 102 条。
