# java.nio.file.attribute.PosixFileAttributeView 设计文档

## 1. API 功能

BasicFileAttributeView + FileOwnerAttributeView 的 POSIX 视图：readAttributes()、
setPermissions(Set)、setGroup(GroupPrincipal)、getOwner/setOwner、继承 setTimes。name() 固定 "posix"。

（签名以目标 JDK javap 实测为准。）

## 2. Java 用户如何使用

getFileAttributeView(path, PosixFileAttributeView.class) 后 chmod/chgrp/utimensat 语义编程化；
与 Files.getOwner/setAttribute("posix:*") 同一通道。

## 3. 开源代码实际用法调研

OpenJDK 回归库与实现源码中的典型用法（自写措辞归纳，不拷贝代码）：
- jdk/test/jdk/java/nio/file/attribute/PosixFileAttributeView/Basic.java：setPermissions/setGroup/setTimes
  往返、异常路径；
- sun.nio.fs.UnixFileAttributeViews$Posix：fchmod/fchown/futimens 封装与属主校验。

版权红线：仅借鉴思路，代码自写；jck8b 严禁拷贝。

## 4. 测试用例设计 + 自动化实现

文件：`PosixFileAttributeViewTest.java`（既有内容全部保留，追加 A12 契约深化 testContract 系列方法）。

契约深化维度：
- 视图可用性与 name、继承矩阵（Basic/Owner/FileAttributeView）；
- setPermissions 5 模式往返 + 集合相等 + null NPE + 缺失文件 NoSuchFileException；
- setTimes null 参数语义（mtime-only、atime-only 不动 mtime、ctime-only 在 Linux 无操作、全 null 无异常）、
  纪元前时间戳往返；
- setGroup 改回自身组（必然成功）往返 + null NPE + ProviderMismatchException（非默认提供者实现）；
- getOwner/setOwner 经 owner 面；缺失文件全负例。
本机实测断言数：37 条（JDK-27 侧 48 条）。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory` + finally 递归删除（cleanup 助手）；权限/属主/隐藏位等
  修改类断言先恢复原值再清理，不留脏状态。
- 平台相关行为（Linux ext4：posix/user/dos 视图可用、acl 视图不可用、dos 属性经 user.DOSATTRIB
  xattr 模拟且 readonly 不被内核强制、creationTime 为模拟值）均在断言处注释说明，按本机实测断言。
- 纯 headless：不依赖 DISPLAY/Locale/TimeZone/墙钟/随机数；不加 @key；不 System.exit。
- 负例一律 checkThrows/checkThrowsIO 断言精确异常类型（IO 抛出路径用 IORunnable 变体）。
