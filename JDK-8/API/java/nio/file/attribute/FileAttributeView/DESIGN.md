# java.nio.file.attribute.FileAttributeView 设计文档

## 1. API 功能

文件属性视图标记接口：extends AttributeView，自身不声明方法；所有具体视图（basic/posix/dos/user/owner/acl）
的共同父类型。

（签名以目标 JDK javap 实测为准。）

## 2. Java 用户如何使用

getFileAttributeView(path, <具体视图>.class) 请求；按需向下转型（posix/dos→basic 可用 setTimes）。

## 3. 开源代码实际用法调研

OpenJDK 回归库与实现源码中的典型用法（自写措辞归纳，不拷贝代码）：
- jdk/test/jdk/java/nio/file/attribute/* 各视图回归用例中的类型协作；
- Files.getFileAttributeView 泛型边界设计（上界本接口）。

版权红线：仅借鉴思路，代码自写；jck8b 严禁拷贝。

## 4. 测试用例设计 + 自动化实现

文件：`FileAttributeViewTest.java`（既有内容全部保留，追加 A12 契约深化 testContract 系列方法）。

契约深化维度：extends AttributeView 且零自有方法、五具体视图 instanceof 矩阵、
继承矩阵（posix/dos extends BasicFileAttributeView、user 非 basic、PosixFileAttributeView extends
FileOwnerAttributeView、AclFileAttributeView extends FileOwnerAttributeView、FileOwner extends
FileAttributeView、FileStoreAttributeView 非 FileAttributeView）、
经父类型面写入子类型视图可见（posix 视图转 basic 面 setTimes，basic 面读到）。
本机实测断言数：21 条（JDK-27 侧 29 条）。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory` + finally 递归删除（cleanup 助手）；权限/属主/隐藏位等
  修改类断言先恢复原值再清理，不留脏状态。
- 平台相关行为（Linux ext4：posix/user/dos 视图可用、acl 视图不可用、dos 属性经 user.DOSATTRIB
  xattr 模拟且 readonly 不被内核强制、creationTime 为模拟值）均在断言处注释说明，按本机实测断言。
- 纯 headless：不依赖 DISPLAY/Locale/TimeZone/墙钟/随机数；不加 @key；不 System.exit。
- 负例一律 checkThrows/checkThrowsIO 断言精确异常类型（IO 抛出路径用 IORunnable 变体）。
