# java.nio.file.attribute.DosFileAttributes 设计文档

## 1. API 功能

DOS 属性只读快照：isReadOnly/isHidden/isArchive/isSystem 四布尔 + 继承 BasicFileAttributes 全部形状。
Linux 上由 dos 视图模拟（user.DOSATTRIB 扩展属性）。

（签名以目标 JDK javap 实测为准。）

## 2. Java 用户如何使用

Files.readAttributes(path, DosFileAttributes.class) 读四布尔；与 DosFileAttributeView setter、
Files.setAttribute("dos:hidden", true) 配合往返。

## 3. 开源代码实际用法调研

OpenJDK 回归库与实现源码中的典型用法（自写措辞归纳，不拷贝代码）：
- jdk/test/jdk/java/nio/file/attribute/DosFileAttributeView/Basic.java：四布尔往返（Windows 平台）；
- sun.nio.fs.LinuxDosFileAttributeView：DOSATTRIB xattr 模拟实现（Linux 侧）。

版权红线：仅借鉴思路，代码自写；jck8b 严禁拷贝。

## 4. 测试用例设计 + 自动化实现

文件：`DosFileAttributesTest.java`（既有内容全部保留，追加 A12 契约深化 testContract 系列方法）。

契约深化维度：新文件四布尔全 false、四布尔经视图 set/clear 往返（8 断言）、instanceof BasicFileAttributes、
size/目录形状、setAttribute/getAttribute("dos:hidden") 往返、"dos:*" map 键集（四 dos 键 + basic 继承键、值 Boolean）、
dos 与 basic 视图 mtime 一致（setTimes 后 FileTime 相等）、Linux readonly 模拟位不被内核强制
（setReadOnly(true) 后 Files.write 仍成功，断言实际行为并注释）、
符号链接 NOFOLLOW 读 dos 属性异常（JDK-8 抛普通 FileSystemException，JDK-27 抛精确子类 FileSystemLoopException——双侧各按实测断言）、FileStore dos 支持位（JDK-8 false / JDK-27 true（版本差异，实测））。
本机实测断言数：39 条（JDK-27 侧 51 条）。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory` + finally 递归删除（cleanup 助手）；权限/属主/隐藏位等
  修改类断言先恢复原值再清理，不留脏状态。
- 平台相关行为（Linux ext4：posix/user/dos 视图可用、acl 视图不可用、dos 属性经 user.DOSATTRIB
  xattr 模拟且 readonly 不被内核强制、creationTime 为模拟值）均在断言处注释说明，按本机实测断言。
- 纯 headless：不依赖 DISPLAY/Locale/TimeZone/墙钟/随机数；不加 @key；不 System.exit。
- 负例一律 checkThrows/checkThrowsIO 断言精确异常类型（IO 抛出路径用 IORunnable 变体）。
