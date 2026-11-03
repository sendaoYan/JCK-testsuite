# java.nio.file.FileSystems 测试方案 (DESIGN)

## 1. API 功能
- 契约: `final class`，纯静态工厂（不可实例化）
- 工厂: `getDefault()`、`getFileSystem(URI)`、`newFileSystem(Path,ClassLoader)`、`newFileSystem(URI,Map<String,?>)`、`newFileSystem(Path,Map<String,?>)`、`newFileSystem(Path,Map,ClassLoader)`

## 2. Java 用户如何使用
- `FileSystems.getDefault()` 取默认文件系统（`getSeparator`、`getPath(...)`、`getRootDirectories`、`provider()`）。
- 组合链路：`getDefault()` → `getSeparator()` 平台分隔符 → `getPath(...)` 构造路径 → 与 `Path.of` 等值；`newFileSystem(URI, Map.of())` 打开 zip/jrt 文件系统。

## 3. 开源代码实际用法调研（真实片段）
- `try (FileSystem zipFO = FileSystems.newFileSystem(uri, Collections.emptyMap())) {` — `jdk25u-dev/test/jdk/tools/sincechecker/SinceChecker.java:287`
- `FileSystem fs = FileSystems.newFileSystem(URI.create("jrt:/"), Map.of());` — `jdk25u-dev/test/jdk/tools/jlink/plugins/SystemModuleDescriptors/src/m4/p4/Main.java:48`
- `FileSystems.getDefault().getPath(this.getPath());` — `jdk25u-dev/src/java.base/share/classes/java/io/File.java:2076`

## 4. 测试用例设计 + 自动化实现（指向 FileSystemsTest.java 黑盒 `testBlackBox`）
- 正常/组合: `getDefault()` 非空且 provider 非空；`getSeparator()` 与 `File.separator` 一致；`getPath("a","b")` 与 `Path.of("a","b")` 等值；`getRootDirectories` 至少一个；`newFileSystem(URI("jrt:/"), Map.of())` 打开 jrt 文件系统后 `getPath("/java.base")` 存在并 close。
- 边界: 无权限/非法 URI 抛异常不崩溃。
- 健壮性: 用 `getDefault().getSeparator()` 而非硬编码 `/`；jrt 文件系统只读检查、用完 close；无全局副作用。
- 白盒 `testWhiteBox`: 反射校验 final class、非抽象、私有构造、getDefault/getFileSystem/newFileSystem 存在。
