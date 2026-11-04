# java.nio.file.attribute.BasicFileAttributes 设计文档

## 1. API 功能

平台无关基础属性只读快照：lastModifiedTime/lastAccessTime/creationTime、isRegularFile/isDirectory/
isSymbolicLink/isOther、size、fileKey。

（签名以目标 JDK javap 实测为准。）

## 2. Java 用户如何使用

Files.readAttributes(path, BasicFileAttributes.class[, LinkOption...]) 一次读取；symlink 语义由
NOFOLLOW_LINKS 控制；fileKey 在 Linux 为 dev+inode 键。

## 3. 开源代码实际用法调研

OpenJDK 回归库与实现源码中的典型用法（自写措辞归纳，不拷贝代码）：
- jdk/test/jdk/java/nio/file/Files/FileAttributes.java：类型矩阵与 readAttributes 对照；
- jdk/test/jdk/java/nio/file/attribute/BasicFileAttributeView/Basic.java：时间戳与 fileKey 断言；
- sun.nio.fs.UnixFileAttributes/UnixFileKey。

版权红线：仅借鉴思路，代码自写；jck8b 严禁拷贝。

## 4. 测试用例设计 + 自动化实现

文件：`BasicFileAttributesTest.java`（既有内容全部保留，追加 A12 契约深化 testContract 系列方法）。

契约深化维度：文件/目录/符号链接（NOFOLLOW true、跟随读目标）类型矩阵、三时间非 null
（ext4 creationTime 为模拟值，只断非 null）、size 精确与增长、fileKey 非 null 且跨读取稳定
（equals/hashCode 一致、对象各异）、空文件 size 0、缺失文件 NoSuchFileException、
NOFOLLOW 读普通文件等价、字符串属性族（basic:isRegularFile/size 默认视图）联动。
本机实测断言数：33 条（JDK-27 侧 50 条）。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory` + finally 递归删除（cleanup 助手）；权限/属主/隐藏位等
  修改类断言先恢复原值再清理，不留脏状态。
- 平台相关行为（Linux ext4：posix/user/dos 视图可用、acl 视图不可用、dos 属性经 user.DOSATTRIB
  xattr 模拟且 readonly 不被内核强制、creationTime 为模拟值）均在断言处注释说明，按本机实测断言。
- 纯 headless：不依赖 DISPLAY/Locale/TimeZone/墙钟/随机数；不加 @key；不 System.exit。
- 负例一律 checkThrows/checkThrowsIO 断言精确异常类型（IO 抛出路径用 IORunnable 变体）。
