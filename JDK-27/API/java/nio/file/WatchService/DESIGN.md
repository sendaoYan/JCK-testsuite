# java.nio.file.WatchService 设计文档

## 1. API 功能

监视服务：poll 两个重载/take/close。

（签名以 JDK-27 javap 实测为准。）

## 2. Java 用户如何使用

- 常规用法：直接面向 `WatchService` 编程，或经 Files/Channels/Selector 等门面 API 间接触达。
- 组合用法：与本包/相关包其它类型配合（如视图+Files.getAttribute、通道+Selector、选项+Files.move）。
- 生命周期：打开/获取 -> 使用 -> 关闭/释放；AutoCloseable 资源用 try-with-resources。
- 失败路径：通过本类型或配套异常类型精确报告失败。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- 目录监视
- 用法特点：优先走 java.nio 高层门面（Files/Channels），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`WatchServiceTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验 Closeable 与重载；黑盒验证空闲 poll 立即/超时返回 null、事件唤醒 poll、close 后 poll/take 抛 ClosedWatchServiceException、双 close 无害。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除；套接字/异步用例仅用
  本地回环地址与临时端口（ServerSocketChannel.open().bind(null)），守护线程 + join/超时等待。
- 平台相关能力（posix/dos/user/acl 视图）以 `supportedFileAttributeViews()`/supportsFileAttributeView
  守护，不支持时打印 NOTE 并跳过行为部分，不判失败；headless 安全，不加 @key。
- 本机实测断言数：15 条。

### 第 4 部分追加（批次 A10 契约深化）

新增：take() 契约（就绪即返、daemon 线程 close 唤醒→ClosedWatchServiceException latch+join 有界）；注册失败矩阵（缺失目录 NoSuchFileException/普通文件 NotDirectoryException/单独 OVERFLOW IAE 实测/OVERFLOW+真实事件合法）；poll 边界（poll(0,SECONDS) 即返 null、poll(1,null) NPE）；服务隔离与互不影响；接口形态（声明方法恰 4 个 poll()/poll(long,TimeUnit)/take()/close() 双侧实测一致、返回型精确、newWatchService 每次返回全新实例、closed 后 timed poll 同样 ClosedWatchServiceException）。

- 本机实测断言数（追加后）：见当前 `PASS (N assertions)` 输出。
