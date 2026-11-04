# java.nio.file.attribute.GroupPrincipal 设计文档

## 1. API 功能

组主体接口：extends UserPrincipal（从而 extends Principal）。

（签名以目标 JDK javap 实测为准。）

## 2. Java 用户如何使用

lookupPrincipalByGroupName 查得；PosixFileAttributes.group 返回、PosixFileAttributeView.setGroup 消费。

## 3. 开源代码实际用法调研

OpenJDK 回归库与实现源码中的典型用法（自写措辞归纳，不拷贝代码）：
- jdk/test/jdk/java/nio/file/attribute/PosixFileAttributeView/Basic.java 的 setGroup 部分；
- sun.nio.fs.UnixUserPrincipals$Group：与 User 类型不同、名字相同亦不等。

版权红线：仅借鉴思路，代码自写；jck8b 严禁拷贝。

## 4. 测试用例设计 + 自动化实现

文件：`GroupPrincipalTest.java`（既有内容全部保留，追加 A12 契约深化 testContract 系列方法）。

契约深化维度：接口层级（UserPrincipal/Principal）、从文件属性发现当前组→lookup 往返、
instanceof 三层、toString==name、重复查找 equals/hashCode、
用户主体与组主体即使同名也不相等（Unix 实现类型区分，对称）、
可赋给 UserPrincipal 使用、setGroup(查得组) 往返、自定义实现 setGroup→ProviderMismatchException、
缺失组名→UserPrincipalNotFoundException（JDK-8 getName 保留所查名；JDK-25+ 实测 getName 返回空串——双侧各按实测断言）、null NPE、空名 UPNFE。
本机实测断言数：23 条（JDK-27 侧 28 条）。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory` + finally 递归删除（cleanup 助手）；权限/属主/隐藏位等
  修改类断言先恢复原值再清理，不留脏状态。
- 平台相关行为（Linux ext4：posix/user/dos 视图可用、acl 视图不可用、dos 属性经 user.DOSATTRIB
  xattr 模拟且 readonly 不被内核强制、creationTime 为模拟值）均在断言处注释说明，按本机实测断言。
- 纯 headless：不依赖 DISPLAY/Locale/TimeZone/墙钟/随机数；不加 @key；不 System.exit。
- 负例一律 checkThrows/checkThrowsIO 断言精确异常类型（IO 抛出路径用 IORunnable 变体）。
