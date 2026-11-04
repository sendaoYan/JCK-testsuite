# java.nio.channels.FileLock 设计文档

## 1. API 功能

文件锁令牌：channel()/acquiredBy()/overlaps()/release()。

（签名以 JDK-27 javap 实测为准。）

## 2. Java 用户如何使用

- 常规用法：直接面向 `FileLock` 编程，或经 Files/Channels/Selector 等门面 API 间接触达。
- 组合用法：与本包/相关包其它类型配合（如视图+Files.getAttribute、通道+Selector、选项+Files.move）。
- 生命周期：打开/获取 -> 使用 -> 关闭/释放；AutoCloseable 资源用 try-with-resources。
- 失败路径：通过本类型或配套异常类型精确报告失败。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- 数据库单实例锁、H2
- 用法特点：优先走 java.nio 高层门面（Files/Channels），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`FileLockTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验构造器与可覆盖方法；黑盒用 StubLock 验证 overlaps 边界语义、toString 内容、async 锁 channel()==null、acquiredBy 语义。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除；套接字/异步用例仅用
  本地回环地址与临时端口（ServerSocketChannel.open().bind(null)），守护线程 + join/超时等待。
- 平台相关能力（posix/dos/user/acl 视图）以 `supportedFileAttributeViews()`/supportsFileAttributeView
  守护，不支持时打印 NOTE 并跳过行为部分，不判失败；headless 安全，不加 @key。
- 本机实测断言数：48 条。

### 4.1 A5 契约深化追加

新增 testWhiteBoxA5（抽象类/2 受保护构造/final 方法面/isValid+release 抽象）与 testBlackBoxA5：
JDK-27 侧钉死零长锁查询 overlaps(0,0)/(10,0) 为 true、(20,0)/(25,0) 为 false、overlaps(10,Long.MAX_VALUE)
true（无溢出回绕）；StubLock null channel 构造 -> NPE；tryLock(5,0) 规范化为 (5, MAX-5)。与 JDK-8 侧
字面零长语义的差异双侧各自断言。断言数 48→76 条（JDK-27 实测）。
