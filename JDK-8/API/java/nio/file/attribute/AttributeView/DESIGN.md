# java.nio.file.attribute.AttributeView 设计文档

## 1. API 功能

属性视图根接口：唯一方法 name()（视图注册名，如 "basic"/"posix"/"user"）。

（签名以目标 JDK javap 实测为准。）

## 2. Java 用户如何使用

作为 FileAttributeView/FileStoreAttributeView 的公共父类型；Files.getFileAttributeView 的类型参数
上界为 FileAttributeView（AttributeView 本身不能直接请求视图）。

## 3. 开源代码实际用法调研

OpenJDK 回归库与实现源码中的典型用法（自写措辞归纳，不拷贝代码）：
- jdk/test/jdk/java/nio/file/Files/FileAttributes.java 等：视图 name 与字符串属性名对拍；
- sun.nio.fs.UnixFileSystemProvider$2 支持视图注册表。

版权红线：仅借鉴思路，代码自写；jck8b 严禁拷贝。

## 4. 测试用例设计 + 自动化实现

文件：`AttributeViewTest.java`（既有内容全部保留，追加 A12 契约深化 testContract 系列方法）。

契约深化维度：唯一方法抽象签名（返回 String 无参）、非泛型、自定义实现、
五个具体视图（basic/posix/dos/user/owner）instanceof + name() 矩阵、
FileStoreAttributeView 是 AttributeView 但非 FileAttributeView、
supportedFileAttributeViews 含全部注册名、
反射白盒：Files.getFileAttributeView 类型参数上界为 FileAttributeView。
本机实测断言数：32 条（JDK-27 侧 40 条）。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory` + finally 递归删除（cleanup 助手）；权限/属主/隐藏位等
  修改类断言先恢复原值再清理，不留脏状态。
- 平台相关行为（Linux ext4：posix/user/dos 视图可用、acl 视图不可用、dos 属性经 user.DOSATTRIB
  xattr 模拟且 readonly 不被内核强制、creationTime 为模拟值）均在断言处注释说明，按本机实测断言。
- 纯 headless：不依赖 DISPLAY/Locale/TimeZone/墙钟/随机数；不加 @key；不 System.exit。
- 负例一律 checkThrows/checkThrowsIO 断言精确异常类型（IO 抛出路径用 IORunnable 变体）。
