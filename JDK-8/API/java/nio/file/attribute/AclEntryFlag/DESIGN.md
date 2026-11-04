# java.nio.file.attribute.AclEntryFlag 设计文档

## 1. API 功能

ACL 继承标志枚举，恰 4 常量：FILE_INHERIT、DIRECTORY_INHERIT、NO_PROPAGATE_INHERIT、INHERIT_ONLY（声明序）。

（签名以目标 JDK javap 实测为准。）

## 2. Java 用户如何使用

setFlags(EnumSet.of(FILE_INHERIT, DIRECTORY_INHERIT)) 控制目录树内 ACL 继承行为。

## 3. 开源代码实际用法调研

OpenJDK 回归库与实现源码中的典型用法（自写措辞归纳，不拷贝代码）：
- jdk/test/jdk/java/nio/file/attribute/AclEntry/Basic.java 标志集断言；
- NFSv4 ACL 语义（RFC 7530 继承标志）在 JDK 文档中的映射说明。

版权红线：仅借鉴思路，代码自写；jck8b 严禁拷贝。

## 4. 测试用例设计 + 自动化实现

文件：`AclEntryFlagTest.java`（既有内容全部保留，追加 A12 契约深化 testContract 系列方法）。

契约深化维度：values() 恰 4 顺序精确、逐常量 name/ordinal/valueOf/toString/declaringClass、
compareTo 声明序、EnumSet allOf/noneOf、valueOf 负例（未知 IAE/大小写 IAE/null NPE）、
Builder 集成（单条目携带全部 4 标志）。
本机实测断言数：37 条（JDK-27 侧 48 条）。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory` + finally 递归删除（cleanup 助手）；权限/属主/隐藏位等
  修改类断言先恢复原值再清理，不留脏状态。
- 平台相关行为（Linux ext4：posix/user/dos 视图可用、acl 视图不可用、dos 属性经 user.DOSATTRIB
  xattr 模拟且 readonly 不被内核强制、creationTime 为模拟值）均在断言处注释说明，按本机实测断言。
- 纯 headless：不依赖 DISPLAY/Locale/TimeZone/墙钟/随机数；不加 @key；不 System.exit。
- 负例一律 checkThrows/checkThrowsIO 断言精确异常类型（IO 抛出路径用 IORunnable 变体）。
