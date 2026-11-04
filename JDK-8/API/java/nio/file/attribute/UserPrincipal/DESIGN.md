# java.nio.file.attribute.UserPrincipal 设计文档

## 1. API 功能

用户主体接口：extends java.security.Principal（继承 getName/implies）；GroupPrincipal 再扩展之。

（签名以目标 JDK javap 实测为准。）

## 2. Java 用户如何使用

UserPrincipalLookupService.lookupPrincipalByName 查得；Files.getOwner、setOwner、PosixFileAttributes.owner
返回该类型；equals 跨次查找值相等（Unix 实现每查新建对象）。

## 3. 开源代码实际用法调研

OpenJDK 回归库与实现源码中的典型用法（自写措辞归纳，不拷贝代码）：
- jdk/test/jdk/java/nio/file/attribute/UserPrincipalLookupService/Basic.java；
- sun.nio.fs.UnixUserPrincipals$User：equals/hashCode 值语义、implies 按 Subject 包含判定。

版权红线：仅借鉴思路，代码自写；jck8b 严禁拷贝。

## 4. 测试用例设计 + 自动化实现

文件：`UserPrincipalTest.java`（既有内容全部保留，追加 A12 契约深化 testContract 系列方法）。

契约深化维度：默认查找服务非 null、当前用户查找非空且 getName==user.name、toString==name（Unix）、
instanceof Principal 且非 GroupPrincipal、重复查找 equals/hashCode 一致对象不同、equals null/异型 false、
Principal.implies 默认语义（null false、空 Subject false、包含自身 true）、
自定义实现契约、文件 owner 与查找结果相等、Files.getOwner 返回本类型。
本机实测断言数：21 条（JDK-27 侧 28 条）。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory` + finally 递归删除（cleanup 助手）；权限/属主/隐藏位等
  修改类断言先恢复原值再清理，不留脏状态。
- 平台相关行为（Linux ext4：posix/user/dos 视图可用、acl 视图不可用、dos 属性经 user.DOSATTRIB
  xattr 模拟且 readonly 不被内核强制、creationTime 为模拟值）均在断言处注释说明，按本机实测断言。
- 纯 headless：不依赖 DISPLAY/Locale/TimeZone/墙钟/随机数；不加 @key；不 System.exit。
- 负例一律 checkThrows/checkThrowsIO 断言精确异常类型（IO 抛出路径用 IORunnable 变体）。
