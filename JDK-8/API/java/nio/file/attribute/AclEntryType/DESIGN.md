# java.nio.file.attribute.AclEntryType 设计文档

## 1. API 功能

ACL 条目类型枚举，恰 4 常量：ALLOW、DENY、AUDIT、ALARM（声明序）。

（签名以目标 JDK javap 实测为准。）

## 2. Java 用户如何使用

setType(AclEntryType.ALLOW/DENY) 决定条目是授权还是拒绝；AUDIT/ALARM 为审计/告警型。

## 3. 开源代码实际用法调研

OpenJDK 回归库与实现源码中的典型用法（自写措辞归纳，不拷贝代码）：
- jdk/test/jdk/java/nio/file/attribute/AclEntry/Basic.java 类型断言；
- NFSv4 ACE 类型语义文档。

版权红线：仅借鉴思路，代码自写；jck8b 严禁拷贝。

## 4. 测试用例设计 + 自动化实现

文件：`AclEntryTypeTest.java`（既有内容全部保留，追加 A12 契约深化 testContract 系列方法）。

契约深化维度：values() 恰 4 顺序精确、逐常量 name/ordinal/valueOf/toString、compareTo 声明序、
EnumSet allOf、valueOf 负例（大小写/未知 IAE、null NPE）、Builder 对 4 类型逐一接受。
本机实测断言数：34 条（JDK-27 侧 52 条）。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory` + finally 递归删除（cleanup 助手）；权限/属主/隐藏位等
  修改类断言先恢复原值再清理，不留脏状态。
- 平台相关行为（Linux ext4：posix/user/dos 视图可用、acl 视图不可用、dos 属性经 user.DOSATTRIB
  xattr 模拟且 readonly 不被内核强制、creationTime 为模拟值）均在断言处注释说明，按本机实测断言。
- 纯 headless：不依赖 DISPLAY/Locale/TimeZone/墙钟/随机数；不加 @key；不 System.exit。
- 负例一律 checkThrows/checkThrowsIO 断言精确异常类型（IO 抛出路径用 IORunnable 变体）。
