# java.nio.file.attribute.DosFileAttributeView 设计文档

## 1. API 功能

DOS 视图：setReadOnly/setHidden/setSystem/setArchive 四布尔 setter + readAttributes() 返回
DosFileAttributes + 继承 BasicFileAttributeView（setTimes）。name() 固定 "dos"。

（签名以目标 JDK javap 实测为准。）

## 2. Java 用户如何使用

getFileAttributeView(path, DosFileAttributeView.class) 设置/清除 DOS 标志；
Linux 上由 DOSATTRIB xattr 模拟，readonly 不影响 open(O_WRONLY)。

## 3. 开源代码实际用法调研

OpenJDK 回归库与实现源码中的典型用法（自写措辞归纳，不拷贝代码）：
- jdk/test/jdk/java/nio/file/attribute/DosFileAttributeView/Basic.java：四 setter 往返与缺文件负例；
- sun.nio.fs.LinuxDosFileAttributeView：xattr 读写与标志位编码。

版权红线：仅借鉴思路，代码自写；jck8b 严禁拷贝。

## 4. 测试用例设计 + 自动化实现

文件：`DosFileAttributeViewTest.java`（既有内容全部保留，追加 A12 契约深化 testContract 系列方法）。

契约深化维度：视图可用 + name "dos" + 继承矩阵（BasicFileAttributeView/FileAttributeView）、
readAttributes 双类型（DosFileAttributes/BasicFileAttributes）、四 setter true/false 往返（8 断言）、
继承 setTimes 经 dos 视图毫秒往返、basic/dos 视图 mtime 一致、
setAttribute/getAttribute("dos:archive") 往返、Linux readonly 不强制（写仍成功且 size 正确，恢复后清理）、
缺失文件 setHidden/setTimes→NoSuchFileException、supportedFileAttributeViews 含 "dos"、
FileStore dos 支持位（JDK-8 false / JDK-27 true（版本差异，实测））。
本机实测断言数：30 条（JDK-27 侧 38 条）。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory` + finally 递归删除（cleanup 助手）；权限/属主/隐藏位等
  修改类断言先恢复原值再清理，不留脏状态。
- 平台相关行为（Linux ext4：posix/user/dos 视图可用、acl 视图不可用、dos 属性经 user.DOSATTRIB
  xattr 模拟且 readonly 不被内核强制、creationTime 为模拟值）均在断言处注释说明，按本机实测断言。
- 纯 headless：不依赖 DISPLAY/Locale/TimeZone/墙钟/随机数；不加 @key；不 System.exit。
- 负例一律 checkThrows/checkThrowsIO 断言精确异常类型（IO 抛出路径用 IORunnable 变体）。
