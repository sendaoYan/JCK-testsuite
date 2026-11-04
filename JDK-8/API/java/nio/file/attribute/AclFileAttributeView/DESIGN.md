# java.nio.file.attribute.AclFileAttributeView 设计文档

## 1. API 功能

ACL 视图接口：getAcl()/setAcl(List<AclEntry>)，extends FileOwnerAttributeView（从而获得
getOwner/setOwner），name() 约定 "acl"。

（签名以目标 JDK javap 实测为准。）

## 2. Java 用户如何使用

NFSv4/ZFS/Windows 文件系统上读写 ACL；Linux ext4 无 ACL 视图（getFileAttributeView 返回 null，
Files.getAttribute("acl:*") 抛 UnsupportedOperationException）。

## 3. 开源代码实际用法调研

OpenJDK 回归库与实现源码中的典型用法（自写措辞归纳，不拷贝代码）：
- jdk/test/jdk/java/nio/file/attribute/AclFileAttributeView/Basic.java（在支持 ACL 的平台上往返）；
- sun.nio.fs.UnixAclFileAttributeView/LinuxFileStore.supportsFileAttributeView 支持矩阵探测。

版权红线：仅借鉴思路，代码自写；jck8b 严禁拷贝。

## 4. 测试用例设计 + 自动化实现

文件：`AclFileAttributeViewTest.java`（既有内容全部保留，追加 A12 契约深化 testContract 系列方法）。

契约深化维度：接口层级链（Acl→Owner→FileAttributeView→AttributeView）、方法面（getAcl 返回 List、
setAcl 收 List、name/getOwner/setOwner 可见）、用户代码可实现性（自定义实现）、
本机 ext4 实证：视图 null、getAttribute/setAttribute("acl:…")→UOE、FileStore.supportsFileAttributeView("acl")=false、
supportedFileAttributeViews 不含 acl、对照 posix=true；防御性支持分支（若文件系统意外支持则 getAcl/setAcl 往返）。
本机实测断言数：23 条（JDK-27 侧 24 条）。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory` + finally 递归删除（cleanup 助手）；权限/属主/隐藏位等
  修改类断言先恢复原值再清理，不留脏状态。
- 平台相关行为（Linux ext4：posix/user/dos 视图可用、acl 视图不可用、dos 属性经 user.DOSATTRIB
  xattr 模拟且 readonly 不被内核强制、creationTime 为模拟值）均在断言处注释说明，按本机实测断言。
- 纯 headless：不依赖 DISPLAY/Locale/TimeZone/墙钟/随机数；不加 @key；不 System.exit。
- 负例一律 checkThrows/checkThrowsIO 断言精确异常类型（IO 抛出路径用 IORunnable 变体）。
