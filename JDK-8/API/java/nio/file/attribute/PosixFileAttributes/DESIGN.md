# java.nio.file.attribute.PosixFileAttributes 设计文档

## 1. API 功能

BasicFileAttributes 的 POSIX 扩展：owner()/group()（UserPrincipal/GroupPrincipal）与
permissions()（Set<PosixFilePermission>，每次调用返回新拷贝）。

（签名以目标 JDK javap 实测为准。）

## 2. Java 用户如何使用

Files.readAttributes(path, PosixFileAttributes.class[, NOFOLLOW_LINKS]) 一次 stat 获得
属主/属组/模式/大小/类型/时间；属主与 UserPrincipalLookupService 结果对拍。

## 3. 开源代码实际用法调研

OpenJDK 回归库与实现源码中的典型用法（自写措辞归纳，不拷贝代码）：
- jdk/test/jdk/java/nio/file/attribute/PosixFileAttributeView/Basic.java：readAttributes 全字段核对；
- sun.nio.fs.UnixFileAttributes：st_uid/st_gid/st_mode 读取与防御拷贝。

版权红线：仅借鉴思路，代码自写；jck8b 严禁拷贝。

## 4. 测试用例设计 + 自动化实现

文件：`PosixFileAttributesTest.java`（既有内容全部保留，追加 A12 契约深化 testContract 系列方法）。

契约深化维度：owner/group 非 null 且名字非空、owner 等于当前用户 lookup、group 是 GroupPrincipal、
permissions 含 OWNER_READ/WRITE、类型矩阵（文件/目录/符号链接 NOFOLLOW 与跟随）、size 精确、
三时间非 null（creationTime 在 ext4 为模拟值）、instanceof BasicFileAttributes、
permissions() 每次新拷贝（clear 不影响后续读取）、setPermissions 后 readAttributes 反映、
缺失文件→NoSuchFileException。
本机实测断言数：29 条（JDK-27 侧 38 条）。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory` + finally 递归删除（cleanup 助手）；权限/属主/隐藏位等
  修改类断言先恢复原值再清理，不留脏状态。
- 平台相关行为（Linux ext4：posix/user/dos 视图可用、acl 视图不可用、dos 属性经 user.DOSATTRIB
  xattr 模拟且 readonly 不被内核强制、creationTime 为模拟值）均在断言处注释说明，按本机实测断言。
- 纯 headless：不依赖 DISPLAY/Locale/TimeZone/墙钟/随机数；不加 @key；不 System.exit。
- 负例一律 checkThrows/checkThrowsIO 断言精确异常类型（IO 抛出路径用 IORunnable 变体）。
