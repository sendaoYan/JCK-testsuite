# java.nio.file.Path 测试方案 (DESIGN)

## 1. API 功能
- 契约: interface，extends `Comparable<Path>` + `Iterable<Path>` + `Watchable`
- 工厂: 通过 `Paths.get(...)` / `Path.of(...)` / `FileSystems.getDefault().getPath(...)` 获取
- 路径分量: `getNameCount`、`getName(int)`、`getFileName`、`getParent`、`getRoot`、`subpath(int,int)`
- 组合: `resolve(String)`、`resolve(Path)`、`resolveSibling`、`relativize`、`normalize`、`toAbsolutePath`、`toRealPath(...)`
- 比较: `startsWith`、`endsWith`、`equals`、`hashCode`、`compareTo`
- 转换: `toString`、`toFile`、`toUri`、`iterator`、`getName(...)`、`of(...)` 静态
- 文件系统: `getFileSystem`

## 2. Java 用户如何使用
- `Path.of("a", "b")` / `Paths.get("a/b")` 构造路径；`resolve` 拼接、`relativize` 求相对、`normalize` 消除 `.`/`..`、`toAbsolutePath` 绝对化。
- 组合链路：构造 → `getNameCount/getName` 取分量 → `subpath` 取子区间 → `resolve` 拼接 → `normalize` → `toFile/toUri` 转换。

## 3. 开源代码实际用法调研（真实片段）
- `Path dir = Files.createTempDirectory(Path.of("."), moduleName);` — `jdk25u-dev/test/jdk/tools/jlink/ClassFileInMetaInfo.java:64`
- `Path copyJdk1Dir = Path.of("./copy-jdk1-tmpdir");` — `jdk25u-dev/test/jdk/tools/jlink/JLinkReproducible3Test.java:51`
- `Path p = Paths.get("");` — `jdk25u-dev/test/jdk/tools/lib/tests/JImageGenerator.java:181`

## 4. 测试用例设计 + 自动化实现（指向 PathTest.java 黑盒 `testBlackBox`）
- 正常/组合: `Path.of("a","b")` 分量 getNameCount/各 getName；subpath 子区间；resolve/relativize 往返；normalize 消除 `.`；startsWith/endsWith/equals/hashCode/compareTo；toAbsolutePath 以根开头；iterator 遍历分量；toFile/toUri 转换。
- 边界: 空路径 `Path.of("").toString()` 为空（JDK 实际行为，见文件内空路径修复）。
- 健壮性: 不依赖文件系统实际存在（纯路径运算）；用 `Path.of`/`getFileSystem().getSeparator()` 而非硬编码 `/`。
- 白盒 `testWhiteBox`: 反射校验 interface、extends Comparable+Iterable+Watchable、of/getNameCount/subpath 等静态/实例方法存在。
