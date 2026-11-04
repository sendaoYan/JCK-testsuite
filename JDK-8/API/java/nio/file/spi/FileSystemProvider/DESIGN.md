# java.nio.file.spi.FileSystemProvider 设计文档

## 1. API 功能

抽象类：文件系统的工厂与操作总线（scheme 寻址 URI、newFileSystem/getFileSystem/getPath、newByteChannel/newDirectoryStream/newInputStream/newOutputStream、createDirectory/delete/copy/move/isSameFile/isHidden/getFileStore/checkAccess/getFileAttributeView/readAttributes/setAttribute、静态 installedProviders）。

（签名以 jdk8u372 javap -p 实测为准。）

## 2. Java 用户如何使用

- 常规用法：Files/FileSystems 门面内部委派到 provider；高级用户经 installedProviders() 拿 "file" provider 直调。
- 生命周期：installedProviders 注册表启动即定；默认 provider 常驻。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- zipfs/jrt 等第三方文件系统接入（实现该抽象类 + ServiceLoader 注册）
- 用法特点：OpenJDK 自身回归（test/jdk/java/nio/file/Files/*、provider 子目录）大量经 Files 间接覆盖 provider 行为；测试侧重 installedProviders 与 URI 寻址负例。

## 4. 测试用例设计 + 自动化实现

文件：`FileSystemProviderTest.java`（白盒 + 黑盒由 main 依次调用）。

设计：
- 白盒：抽象类/protected 构造（JDK-8 另有 private Void 安全门构造，实测差异）/抽象方法抽查/installedProviders 静态。
- 注册表：非空、含 "file"、jar(zipfs) 存在（实测）、JDK-8 无 "jrt"（JDK-27 有，双侧各自断言）、默认 fs 与之同对象。
- URI 寻址：getPath(file:///tmp/x)==/tmp/x、getPath(toUri()) 往返、mailto IAE、带 authority IAE、null NPE、newFileSystem(file:///) FileSystemAlreadyExistsException、newFileSystem(mailto) IAE、getFileSystem(file:///)==默认 fs 且 isOpen、getFileSystem(file:///tmp) IAE（仅根）、getFileSystem(https) IAE。
- 操作面（临时目录树真实调用）：newByteChannel 读内容+size、newInputStream 全量、newOutputStream 写、newDirectoryStream 过滤、createDirectory 成功/已存在 FileAlreadyExistsException/父缺失 NoSuchFileException、delete 成功/缺失 NoSuchFileException/null NPE、copy/move 内容与存在性、isSameFile 真/假/null NPE、readAttributes 类版+String 版（size 为 Long 实测）+view+setAttribute mtime round-trip+null NPE、checkAccess READ+WRITE 过/EXECUTE 不可执行 AccessDeniedException/缺失 NoSuchFileException/null NPE、isHidden 点文件 Unix 约定、getFileStore name/type 非空、newFileSystem(Path,Map) UOE。
- 跨 provider：zipfs 路径喂 file provider → ProviderMismatchException（readAttributes/delete 两处）。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 临时目录 + finally 递归清理；负例 expect(精确异常类型, IoAction)。
- 版本差异（javap+实测）：JDK-27 新增 exists(Path,LinkOption...)/readAttributesIfExists(Path,Class,LinkOption...)（JDK-8 断言其不存在）；JDK-27 installedProviders 含 "jrt"；JDK-8 spi 类有 private Void 安全门构造（JDK-27 无）。
