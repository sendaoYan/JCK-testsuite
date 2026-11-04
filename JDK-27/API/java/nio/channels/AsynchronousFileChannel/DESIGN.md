# java.nio.channels.AsynchronousFileChannel 设计文档

## 1. API 功能

异步文件通道：位置化读写、truncate/force、lock/tryLock。

（签名以 JDK-27 javap 实测为准。）

## 2. Java 用户如何使用

- 常规用法：直接面向 `AsynchronousFileChannel` 编程，或经 Files/Channels/Selector 等门面 API 间接触达。
- 组合用法：与本包/相关包其它类型配合（如视图+Files.getAttribute、通道+Selector、选项+Files.move）。
- 生命周期：打开/获取 -> 使用 -> 关闭/释放；AutoCloseable 资源用 try-with-resources。
- 失败路径：通过本类型或配套异常类型精确报告失败。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- AIO 文件日志
- 用法特点：优先走 java.nio 高层门面（Files/Channels），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`AsynchronousFileChannelTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验抽象方法；黑盒验证位置化 Future/handler 读写、越界读 -1、锁 0..MAX_VALUE、重叠锁同步抛 OverlappingFileLockException、缺 READ 打开抛 NoSuchFileException。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除；套接字/异步用例仅用
  本地回环地址与临时端口（ServerSocketChannel.open().bind(null)），守护线程 + join/超时等待。
- 平台相关能力（posix/dos/user/acl 视图）以 `supportedFileAttributeViews()`/supportsFileAttributeView
  守护，不支持时打印 NOTE 并跳过行为部分，不判失败；headless 安全，不加 @key。
- 本机实测断言数：34 条。

### A7 批次深化（契约覆盖增强，append-only，原断言全部保留）

- 新增方法：testOpenOptionsContract/testReadWriteContract/testLockContract/testSurfaceContract/testPooledOpenContract。
- 负例矩阵（精确异常类型，同步抛 vs 经 Future/handler 交付分开断言）：IllegalArgumentException（负 position、只读缓冲写）、NonWritableChannelException（只读打开后写/独占锁）、NonReadableChannelException、OverlappingFileLockException（同 JVM 重叠锁）、ClosedChannelException 经 Future cause 交付（closed 后 read/write/lock 均非同步抛）
- 确定性与健壮性：稀疏洞算术（写入 pos 超过 size → 中间零填充）逐字节断言；锁 valid 到通道 close 即失效；临时目录 finally 递归清理。
- 版本差异（按侧断言）：closed 通道的 read/write/lock 全部经 Future 交付 ClosedChannelException，不同步抛；独占锁在只读通道上抛 NonWritableChannelException。
- 本机实测断言数：27 → 109 条。
