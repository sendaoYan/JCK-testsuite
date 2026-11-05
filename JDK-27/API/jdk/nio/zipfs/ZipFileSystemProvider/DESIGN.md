# jdk.nio.zipfs.ZipFileSystemProvider 契约设计

## 1. API 功能
`ZipFileSystemProvider extends FileSystemProvider`，scheme 为 `jar`（也即 `jar:file:` URL 的协议端点）。核心 `newFileSystem(Path, Map<String,?>)` 构造 ZipFileSystem：按需读中央目录、支持写回（默认 copy 属性）、close 时把新增 entry 物化进 zip 文件。其余 delete/move/copy 等由 ZipFileSystem 的 Files 门面承接。

## 2. Java 用户如何使用
`FileSystems.newFileSystem(path, map)` 在普通场景下转发到本 provider；直接反射实例化（无参构造器）后可显式 `newFileSystem(path, Map.of())` 获得同一行为。map 可带 `create=true`（新建 zip）、`encoding`、`useTempFile`、`noCompressionT` 等 zipfs 私有属性。

## 3. 开源代码实际用法调研
- OpenJDK 自身：`FileSystems.newFileSystem` 与 `jar:` URLStreamHandler 都路由到本类。
- Gradle/IntelliJ 用 zipfs 做只读资源包扫描；真实写回（新增 entry 持久化）被 Maven Shade/zipflinger(Android build) 类工具用于增量改包。

## 4. 测试用例设计+自动化实现
- scheme 精确 "jar" 且两次调用稳定；反射构造（@modules `jdk.zipfs/jdk.nio.zipfs:+open`）。
- 真实读写闭环（testDeepen）：ZipOutputStream 预制 zip（dir/hello.txt=6 字节）→ newFileSystem → isOpen、separator "/"、entry 存在/size/内容逐字节 round-trip → 经 Files.newOutputStream 写 /added.txt=5 字节 → close → 重开断言 "added" 已持久化、原 entry 完好（真实 zip 结构写回，非 mock）。
- 自动化：jtreg `run main` + `check()`；临时文件 deleteOnExit。
