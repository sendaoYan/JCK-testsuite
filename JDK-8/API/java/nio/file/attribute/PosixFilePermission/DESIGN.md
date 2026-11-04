# java.nio.file.attribute.PosixFilePermission 设计文档

## 1. API 功能

POSIX 权限位枚举，恰 9 个常量（owner/group/others × read/write/exec），声明顺序即
OWNER_READ..OTHERS_EXECUTE，枚举序数固定。

（签名以目标 JDK javap 实测为准。）

## 2. Java 用户如何使用

作为 PosixFilePermissions/PosixFileAttributeView 的元素类型：EnumSet 组合、contains 判断、
Files.getAttribute("posix:permissions") 返回的 Set 元素。

## 3. 开源代码实际用法调研

OpenJDK 回归库与实现源码中的典型用法（自写措辞归纳，不拷贝代码）：
- jdk/test/jdk/java/nio/file/attribute/PosixFileAttributeView/Basic.java 中权限集断言以本枚举常量表达；
- sun.nio.fs.UnixConstants：底层 S_IRUSR 等位与枚举映射。

版权红线：仅借鉴思路，代码自写；jck8b 严禁拷贝。

## 4. 测试用例设计 + 自动化实现

文件：`PosixFilePermissionTest.java`（既有内容全部保留，追加 A12 契约深化 testContract 系列方法）。

契约深化维度：values() 恰 9 且顺序精确、逐常量 name()/ordinal()/valueOf()/toString()/getDeclaringClass()、
compareTo 按声明序、isEnum/getEnumConstants、valueOf 负例（大小写敏感/未知常量 IAE/null NPE）、
EnumSet allOf/noneOf、与权限串编解码集成（rwxrwxrwx 含全部 9 常量）。
本机实测断言数：90 条（JDK-27 侧 99 条）。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory` + finally 递归删除（cleanup 助手）；权限/属主/隐藏位等
  修改类断言先恢复原值再清理，不留脏状态。
- 平台相关行为（Linux ext4：posix/user/dos 视图可用、acl 视图不可用、dos 属性经 user.DOSATTRIB
  xattr 模拟且 readonly 不被内核强制、creationTime 为模拟值）均在断言处注释说明，按本机实测断言。
- 纯 headless：不依赖 DISPLAY/Locale/TimeZone/墙钟/随机数；不加 @key；不 System.exit。
- 负例一律 checkThrows/checkThrowsIO 断言精确异常类型（IO 抛出路径用 IORunnable 变体）。
