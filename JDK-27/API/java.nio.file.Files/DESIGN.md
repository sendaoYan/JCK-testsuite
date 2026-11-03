# java.nio.file.Files 测试方案 (DESIGN)

## 1. API 功能
- 契约: `final class`，纯静态工具（不可实例化）
- 创建/删除: `createFile`、`createDirectory`、`createDirectories`、`createTempFile`、`createTempDirectory`、`delete`、`deleteIfExists`
- 读写: `readAllBytes`、`readString`、`write(byte[])`、`writeString`、`newInputStream`、`newOutputStream`、`newBufferedReader`、`newBufferedWriter`
- 拷贝/移动: `copy`、`move`
- 属性: `size`、`exists`、`notExists`、`isDirectory`、`isRegularFile`、`isReadable`、`isWritable`、`isExecutable`、`getLastModifiedTime`、`getAttribute`、`readAttributes`
- 目录: `list`、`walk`、`walkFileTree`、`newDirectoryStream`
- 流: `lines`、`readAllLines`、`write(...)` 等

## 2. Java 用户如何使用
- `Files.writeString(p, "text")` 写文本、`Files.readString(p)` 读回；`Files.createTempDirectory("jck")` 建独立临时目录；`Files.copy(src,dst)` 拷贝。
- 组合链路：createTempDirectory → writeString → readString/size → copy → move → list → deleteIfExists 清理。

## 3. 开源代码实际用法调研（真实片段）
- `String moduleInfoContent = Files.readString(moduleInfoFile);` — `jdk25u-dev/test/jdk/tools/sincechecker/SinceChecker.java:359`
- `Files.writeString(moduleDir.resolve("module-info.java"), moduleInfoContent.toString());` — `jdk25u-dev/test/jdk/tools/jlink/JLink100Modules.java:75`
- `Files.copy(file, dst.resolve(src.relativize(file)), StandardCopyOption.COPY_ATTRIBUTES);` — `jdk25u-dev/test/jdk/tools/jlink/JLinkReproducible3Test.java:94`
- `Path dir = Files.createTempDirectory(Path.of("."), moduleName);` — `jdk25u-dev/test/jdk/tools/jlink/ClassFileInMetaInfo.java:64`

## 4. 测试用例设计 + 自动化实现（指向 FilesTest.java 黑盒 `testBlackBox`）
- 正常/组合: 独立临时目录内 createDirectory/createDirectories → writeString → readString 精确 → size → copy → move → list（排序）→ isDirectory/isRegularFile/exists/notExists → deleteIfExists 清理；createTempFile/createTempDirectory 命名。
- 边界: 删除不存在 `deleteIfExists` 返回 false；读不存在抛 `NoSuchFileException`。
- 健壮性: 全程 `Files.createTempDirectory("jck-files")` + finally 递归清理；目录列举先排序；显式 UTF-8（readString/writeString 默认 UTF-8）；无墙钟依赖（lastModified 仅断言非负）。
- 白盒 `testWhiteBox`: 反射校验 final class、非抽象、私有构造、readString/writeString/copy/move/createTempDirectory 等存在。
