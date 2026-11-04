# java.nio.file.FileSystems 设计文档

## 1. API 功能

文件系统工厂 (final): getDefault/getFileSystem(URI)/newFileSystem(URI,Map)。

（签名以 jdk8u372 javap -public 实测为准。）

## 2. Java 用户如何使用

- 常规用法: getDefault 取默认 FS；newFileSystem(jar:URI,env) 打开可关闭的 zip 文件系统并读写其条目。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- FileSystems
- 用法特点：优先走 java.nio 高层门面（Files/Channels/Selector），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`FileSystemsTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验 final/extends Object/三方法签名；黑盒验证默认 FS 属性、getFileSystem(file URI) 返回同一实例、zip 文件系统打开/读条目/size/关闭。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除。
- 平台相关能力（权限/符号链接/watch 事件）以运行时能力守护，不支持时打印 NOTE 跳过，不判失败；
  headless 安全，不加 @key。
- 本机实测断言数：17 条。

## 深化追加（契约级，批次 A9）

新增维度（断言 15 → 39）：getFileSystem/newFileSystem 各 URI 精确异常矩阵：
newFileSystem(file:) -> FileSystemAlreadyExistsException（探针修正，任务书猜 IAE）；
getFileSystem(file:///nope) -> IllegalArgumentException；bogus: -> ProviderNotFoundException；
jar: 未打开 -> FileSystemNotFoundException；newFileSystem((URI)null, ...) -> NPE。
zipfs 双侧可用：newFileSystem(zipPath, (ClassLoader) null) 打开 -> "/" 根目录 -> 关闭。

版本差异：JDK-8 有 newFileSystem(Path, ClassLoader) 但无 newFileSystem(Path)/
newFileSystem(Path, Map)（8 侧反射断言不存在，27 侧实测可用）。
