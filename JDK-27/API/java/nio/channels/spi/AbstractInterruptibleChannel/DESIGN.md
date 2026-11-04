# java.nio.channels.spi.AbstractInterruptibleChannel 设计文档

## 1. API 功能

可中断通道骨架：begin/end 协议与 implCloseChannel。

（签名以 JDK-27 javap 实测为准。）

## 2. Java 用户如何使用

- 常规用法：直接面向 `AbstractInterruptibleChannel` 编程，或经 Files/Channels/Selector 等门面 API 间接触达。
- 组合用法：与本包/相关包其它类型配合（如视图+Files.getAttribute、通道+Selector、选项+Files.move）。
- 生命周期：打开/获取 -> 使用 -> 关闭/释放；AutoCloseable 资源用 try-with-resources。
- 失败路径：通过本类型或配套异常类型精确报告失败。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- 自定义可中断通道
- 用法特点：优先走 java.nio 高层门面（Files/Channels），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`AbstractInterruptibleChannelTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验 final close/isOpen 与 protected begin/end；黑盒用 StubChannel 验证 begin/end 协议、close 只调一次 implCloseChannel、end(false) 抛 AsynchronousCloseException、FileChannel 同契约。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除；套接字/异步用例仅用
  本地回环地址与临时端口（ServerSocketChannel.open().bind(null)），守护线程 + join/超时等待。
- 平台相关能力（posix/dos/user/acl 视图）以 `supportedFileAttributeViews()`/supportsFileAttributeView
  守护，不支持时打印 NOTE 并跳过行为部分，不判失败；headless 安全，不加 @key。
- 本机实测断言数：18 条。

### A7 批次深化（契约覆盖增强，append-only，原断言全部保留）

- 新增方法：testStructureContract/testCloseLifecycleContract/testBeginEndContract/testSelfInterruptContract/testOtherThreadInterruptContract/testRealOperationInterruptContract。
- 负例矩阵（精确异常类型，同步抛 vs 经 Future/handler 交付分开断言）：close 幂等且 implCloseChannel 恰好执行一次；另一线程在 begin/end 窗口内中断 → ClosedByInterruptException 且通道关闭（双侧确定）；真实阻塞读被中断同样精确抛出
- 确定性与健壮性：窗口法：helper 线程 begin→CountDownLatch 停留→主线程 interrupt→end(false)，全部有界等待、线程守护。
- 版本差异（按侧断言）：自中断差异：JDK-8 在 begin/end 窗口内 Thread.currentThread().interrupt() 会使 end(false)/end(true) 抛 ClosedByInterruptException 并关闭通道；JDK-27 不抛、通道保持打开（end 只上报真实 park 中收到的中断）。两条路径各自按侧断言。
- 本机实测断言数：18 → 57 条。
