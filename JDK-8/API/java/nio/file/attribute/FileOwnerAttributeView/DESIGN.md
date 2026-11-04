# java.nio.file.attribute.FileOwnerAttributeView 设计文档

## 1. API 功能

属主视图：getOwner()/setOwner(UserPrincipal)，extends FileAttributeView；Linux 上独立请求时 name()=="owner"。

（签名以目标 JDK javap 实测为准。）

## 2. Java 用户如何使用

Files.getFileAttributeView(path, FileOwnerAttributeView.class) 或经 posix 视图读写属主；
Files.getOwner/setOwner 与 setAttribute("owner:owner"/"posix:owner") 为便捷通道。

## 3. 开源代码实际用法调研

OpenJDK 回归库与实现源码中的典型用法（自写措辞归纳，不拷贝代码）：
- jdk/test/jdk/java/nio/file/attribute/PosixFileAttributeView/Basic.java 的 owner 部分；
- sun.nio.fs.UnixFileAttributeViews：fchown 封装与属主类型校验（UnixUserPrincipal 否则 ProviderMismatch）。

版权红线：仅借鉴思路，代码自写；jck8b 严禁拷贝。

## 4. 测试用例设计 + 自动化实现

文件：`FileOwnerAttributeViewTest.java`（既有内容全部保留，追加 A12 契约深化 testContract 系列方法）。

契约深化维度：视图可用 + name "owner" + instanceof FileAttributeView、
getOwner 等于当前用户 lookup、setOwner 自身往返（必然成功）、setOwner(null) NPE、
外来实现属主→ProviderMismatchException、缺失文件 get/setOwner→NoSuchFileException、
posix 视图的 owner 面（instanceof、get/set 往返、name 保持 "posix"）、
Files.getOwner/setOwner 往返、setAttribute/getAttribute("owner:owner"、"posix:owner") 往返、目录视图。
本机实测断言数：23 条（JDK-27 侧 33 条）。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory` + finally 递归删除（cleanup 助手）；权限/属主/隐藏位等
  修改类断言先恢复原值再清理，不留脏状态。
- 平台相关行为（Linux ext4：posix/user/dos 视图可用、acl 视图不可用、dos 属性经 user.DOSATTRIB
  xattr 模拟且 readonly 不被内核强制、creationTime 为模拟值）均在断言处注释说明，按本机实测断言。
- 纯 headless：不依赖 DISPLAY/Locale/TimeZone/墙钟/随机数；不加 @key；不 System.exit。
- 负例一律 checkThrows/checkThrowsIO 断言精确异常类型（IO 抛出路径用 IORunnable 变体）。
