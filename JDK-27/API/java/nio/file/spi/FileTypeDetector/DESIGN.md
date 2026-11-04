# java.nio.file.spi.FileTypeDetector 设计文档

## 1. API 功能

内容类型探测 SPI：probeContentType(Path)。

（签名以 JDK-27 javap 实测为准。）

## 2. Java 用户如何使用

- 常规用法：直接面向 `FileTypeDetector` 编程，或经 Files/Channels/Selector 等门面 API 间接触达。
- 组合用法：与本包/相关包其它类型配合（如视图+Files.getAttribute、通道+Selector、选项+Files.move）。
- 生命周期：打开/获取 -> 使用 -> 关闭/释放；AutoCloseable 资源用 try-with-resources。
- 失败路径：通过本类型或配套异常类型精确报告失败。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- MIME 探测
- 用法特点：优先走 java.nio 高层门面（Files/Channels），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`FileTypeDetectorTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验抽象与保护构造器；黑盒用固定回答子类与 null 子类验证契约、Files.probeContentType 平台探测 .txt 得 text/* 或 null。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除；套接字/异步用例仅用
  本地回环地址与临时端口（ServerSocketChannel.open().bind(null)），守护线程 + join/超时等待。
- 平台相关能力（posix/dos/user/acl 视图）以 `supportedFileAttributeViews()`/supportsFileAttributeView
  守护，不支持时打印 NOTE 并跳过行为部分，不判失败；headless 安全，不加 @key。
- 本机实测断言数：9 条。

### 第 4 部分追加（批次 A10 契约深化）

新增：子类契约（调用计数、IOException 同对象透传、null 路径由子类自定——接口无检查实测）；Files 委派精确矩阵（.txt/.pdf/.html/.png 精确值、未知扩展 null、缺失文件不抛、扩展名优先、未注册 detector 不被咨询、目录恒 null——JDK-8 为 inode/directory，差异记录）。

- 本机实测断言数（追加后）：见当前 `PASS (N assertions)` 输出。
