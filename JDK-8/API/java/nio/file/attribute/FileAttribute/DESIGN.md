# java.nio.file.attribute.FileAttribute 设计文档

## 1. API 功能

属性名/值载体接口（泛型）：name() 返回视图限定属性名（如 "posix:permissions"），value() 返回值；
接口不做任何校验（null 亦通过），由消费方解释。

（签名以目标 JDK javap 实测为准。）

## 2. Java 用户如何使用

Files.createFile/createDirectory/CreateSymbolicLink 的可变参数；最常用 PosixFilePermissions.asFileAttribute；
也可自定义实现直喂 Files.createFile。

## 3. 开源代码实际用法调研

OpenJDK 回归库与实现源码中的典型用法（自写措辞归纳，不拷贝代码）：
- jdk/test/jdk/java/nio/file/Files/CreateFile.java 等：自定义 FileAttribute 传入路径；
- sun.nio.fs.UnixFileSystemProvider.createFile：按 attr.name() 前缀分发、未知视图→UnsupportedOperationException。

版权红线：仅借鉴思路，代码自写；jck8b 严禁拷贝。

## 4. 测试用例设计 + 自动化实现

文件：`FileAttributeTest.java`（既有内容全部保留，追加 A12 契约深化 testContract 系列方法）。

契约深化维度：两方法均抽象、单类型参数泛型、自定义实现 name/value 原样往返（含 null 直通）、
value() 可惰性计算且可重复调用、asFileAttribute 即 FileAttribute（name 限定名/值集合/稳定）、
Files.createFile 消费自定义属性（模式精确施加 rw-r-----）、
未知视图名属性→UOE（create 时拒绝）、空属性数组=默认模式。
本机实测断言数：19 条（JDK-27 侧 26 条）。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory` + finally 递归删除（cleanup 助手）；权限/属主/隐藏位等
  修改类断言先恢复原值再清理，不留脏状态。
- 平台相关行为（Linux ext4：posix/user/dos 视图可用、acl 视图不可用、dos 属性经 user.DOSATTRIB
  xattr 模拟且 readonly 不被内核强制、creationTime 为模拟值）均在断言处注释说明，按本机实测断言。
- 纯 headless：不依赖 DISPLAY/Locale/TimeZone/墙钟/随机数；不加 @key；不 System.exit。
- 负例一律 checkThrows/checkThrowsIO 断言精确异常类型（IO 抛出路径用 IORunnable 变体）。
