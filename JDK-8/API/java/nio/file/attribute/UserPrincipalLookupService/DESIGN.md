# java.nio.file.attribute.UserPrincipalLookupService 设计文档

## 1. API 功能

抽象查找服务：lookupPrincipalByName(String)/lookupPrincipalByGroupName(String)，IOException，
未知主体→UserPrincipalNotFoundException；由 FileSystem 提供（默认文件系统每 fs 单例）。

（签名以目标 JDK javap 实测为准。）

## 2. Java 用户如何使用

FileSystems.getDefault().getUserPrincipalLookupService() 后按名查用户/组，结果喂 setOwner/setGroup。

## 3. 开源代码实际用法调研

OpenJDK 回归库与实现源码中的典型用法（自写措辞归纳，不拷贝代码）：
- jdk/test/jdk/java/nio/file/attribute/UserPrincipalLookupService/Basic.java：正反例矩阵；
- sun.nio.fs.UnixUserPrincipalLookupService：getpwnam/getgrnam 封装与异常映射。

版权红线：仅借鉴思路，代码自写；jck8b 严禁拷贝。

## 4. 测试用例设计 + 自动化实现

文件：`UserPrincipalLookupServiceTest.java`（既有内容全部保留，追加 A12 契约深化 testContract 系列方法）。

契约深化维度：服务非 null 且同 fs 同实例、抽象类白盒、
当前用户查找（非空/名字往返/重复等值）、组查找（文件属性发现组名/名字往返/重复等值）、
未知用户/组→UPNFE（JDK-8 getName 保留所查名；JDK-25+ 实测返回空串——双侧各按实测断言）、空用户名→UPNFE、null 用户/组→NPE、
自定义子类：两抽象方法调用记录、IOException 透传、toString 非 null。
本机实测断言数：22 条（JDK-27 侧 31 条）。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory` + finally 递归删除（cleanup 助手）；权限/属主/隐藏位等
  修改类断言先恢复原值再清理，不留脏状态。
- 平台相关行为（Linux ext4：posix/user/dos 视图可用、acl 视图不可用、dos 属性经 user.DOSATTRIB
  xattr 模拟且 readonly 不被内核强制、creationTime 为模拟值）均在断言处注释说明，按本机实测断言。
- 纯 headless：不依赖 DISPLAY/Locale/TimeZone/墙钟/随机数；不加 @key；不 System.exit。
- 负例一律 checkThrows/checkThrowsIO 断言精确异常类型（IO 抛出路径用 IORunnable 变体）。
