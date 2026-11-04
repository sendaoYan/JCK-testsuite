# java.nio.file.spi.FileSystemProvider 设计文档

## 1. API 功能

provider SPI：installedProviders、scheme、newFileSystem/getFileSystem/getPath 及全套文件操作。

（签名以 JDK-27 javap 实测为准。）

## 2. Java 用户如何使用

- 常规用法：直接面向 `FileSystemProvider` 编程，或经 Files/Channels/Selector 等门面 API 间接触达。
- 组合用法：与本包/相关包其它类型配合（如视图+Files.getAttribute、通道+Selector、选项+Files.move）。
- 生命周期：打开/获取 -> 使用 -> 关闭/释放；AutoCloseable 资源用 try-with-resources。
- 失败路径：通过本类型或配套异常类型精确报告失败。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- 自定义文件系统
- 用法特点：优先走 java.nio 高层门面（Files/Channels），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`FileSystemProviderTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验抽象方法面；黑盒验证 file/jar provider 安装、newByteChannel/newDirectoryStream/createDirectory/delete/copy/move/isSameFile/属性读写/checkAccess/isHidden/流桥接逐一经默认 provider 触发、外来路径抛 ProviderMismatchException、getFileSystem 仅认 file:///、默认 fs 二次创建抛 FileSystemAlreadyExistsException。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除；套接字/异步用例仅用
  本地回环地址与临时端口（ServerSocketChannel.open().bind(null)），守护线程 + join/超时等待。
- 平台相关能力（posix/dos/user/acl 视图）以 `supportedFileAttributeViews()`/supportsFileAttributeView
  守护，不支持时打印 NOTE 并跳过行为部分，不判失败；headless 安全，不加 @key。
- 本机实测断言数：44 条。

### 第 4 部分追加（批次 A10 契约深化）

新增：JDK-27 专属面（exists(Path,LinkOption...) 四情形、readAttributesIfExists 两情形、方法存在性断言；仅一个 protected 构造无安全门）；注册表（jar+jrt 存在实测、默认 fs 同对象）；URI/负例/属性全矩阵（getPath mailto·authority IAE、null NPE、newFileSystem mailto IAE、getFileSystem 仅根/https IAE、==默认 fs、delete/copy/isSameFile/readAttributes/checkAccess null NPE、createDirectory 已存在/父缺失、delete 缺失、EXECUTE 不可执行 AccessDenied、isHidden 点文件、getFileStore name/type、readAttributes(String) size 为 Long 实测、newFileSystem(Path,Map) UOE、newInputStream 全量）。

- 本机实测断言数（追加后）：见当前 `PASS (N assertions)` 输出。
