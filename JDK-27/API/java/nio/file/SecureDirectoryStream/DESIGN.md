# java.nio.file.SecureDirectoryStream 设计文档

## 1. API 功能

相对路径操作的目录流：newByteChannel/move/deleteFile/deleteDirectory/属性视图。

（签名以 JDK-27 javap 实测为准。）

## 2. Java 用户如何使用

- 常规用法：直接面向 `SecureDirectoryStream` 编程，或经 Files/Channels/Selector 等门面 API 间接触达。
- 组合用法：与本包/相关包其它类型配合（如视图+Files.getAttribute、通道+Selector、选项+Files.move）。
- 生命周期：打开/获取 -> 使用 -> 关闭/释放；AutoCloseable 资源用 try-with-resources。
- 失败路径：通过本类型或配套异常类型精确报告失败。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- 防 TOCTOU 安全文件操作
- 用法特点：优先走 java.nio 高层门面（Files/Channels），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`SecureDirectoryStreamTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验方法面；黑盒（Linux 默认支持）验证相对建读文件、嵌套流列举、路径绑定视图、move/deleteFile/deleteDirectory、缺失文件 NoSuchFileException、关闭后操作抛 ClosedDirectoryStreamException。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除；套接字/异步用例仅用
  本地回环地址与临时端口（ServerSocketChannel.open().bind(null)），守护线程 + join/超时等待。
- 平台相关能力（posix/dos/user/acl 视图）以 `supportedFileAttributeViews()`/supportsFileAttributeView
  守护，不支持时打印 NOTE 并跳过行为部分，不判失败；headless 安全，不加 @key。
- 本机实测断言数：19 条。

## 深化追加（契约级，批次 A9）

新增维度（断言 15 → 31）：testDeepened（注入）：相对 READ 通道往返、NOFOLLOW_LINKS 视图、嵌套
NOFOLLOW 流、负例精确类型（deleteDirectory 非空 -> DirectoryNotEmptyException、缺失 ->
NoSuchFileException、deleteFile 目录 -> FileSystemException）、close 后 deleteFile ->
ClosedDirectoryStreamException；实测修正：getFileAttributeView 在流 close 后返回惰性视图不抛
（任务书假设抛异常被推翻，改断言惰性语义）。
