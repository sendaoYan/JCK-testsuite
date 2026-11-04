# java.nio.file.attribute.FileStoreAttributeView 设计文档

## 1. API 功能

存储级属性视图标记接口：extends AttributeView，不声明方法（区别于文件级 FileAttributeView）；
Linux ext4 不发布任何 FileStoreAttributeView（空间统计走 FileStore.getAttribute）。

（签名以目标 JDK javap 实测为准。）

## 2. Java 用户如何使用

FileStore.getFileStoreAttributeView(Class) 请求（ext4 上 FileStoreAttributeView.class→null）；
总量/可用量经 getAttribute("totalSpace"/"usableSpace"/"unallocatedSpace")。

## 3. 开源代码实际用法调研

OpenJDK 回归库与实现源码中的典型用法（自写措辞归纳，不拷贝代码）：
- jdk/test/jdk/java/nio/file/attribute/FileStore/… 空间属性与视图支持矩阵用例；
- sun.nio.fs.LinuxFileStore：视图支持矩阵与 getAttribute 实现（未知属性→UOE）。

版权红线：仅借鉴思路，代码自写；jck8b 严禁拷贝。

## 4. 测试用例设计 + 自动化实现

文件：`FileStoreAttributeViewTest.java`（既有内容全部保留，追加 A12 契约深化 testContract 系列方法）。

契约深化维度：标记接口形状（零自有方法、非 FileAttributeView）、自定义实现、
本机实证：FileStoreAttributeView.class→null（ext4）、getFileStoreAttributeView(null)→NPE、
store name/type/toString 非空、totalSpace>0 / usableSpace、unallocatedSpace≥0 均为 Long、
getAttribute("bogus")→UOE、supportsFileAttributeView 矩阵（basic/posix true、acl false、
Class 版本 true）与 null NPE（String/Class 两态）、isReadOnly false。
本机实测断言数：25 条（JDK-27 侧 29 条）。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory` + finally 递归删除（cleanup 助手）；权限/属主/隐藏位等
  修改类断言先恢复原值再清理，不留脏状态。
- 平台相关行为（Linux ext4：posix/user/dos 视图可用、acl 视图不可用、dos 属性经 user.DOSATTRIB
  xattr 模拟且 readonly 不被内核强制、creationTime 为模拟值）均在断言处注释说明，按本机实测断言。
- 纯 headless：不依赖 DISPLAY/Locale/TimeZone/墙钟/随机数；不加 @key；不 System.exit。
- 负例一律 checkThrows/checkThrowsIO 断言精确异常类型（IO 抛出路径用 IORunnable 变体）。
