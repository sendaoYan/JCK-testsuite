# java.nio.file.Paths 测试方案 (DESIGN)

## 1. API 功能
- 契约: `final class`，纯静态工厂（不可实例化，私有构造）
- 工厂: `get(String,String...)`、`get(URI)`

## 2. Java 用户如何使用
- `Paths.get("a", "b", "c")` 用默认文件系统把字符串序列拼成 Path；`Paths.get("a/b")` 单串按平台分隔符解析。
- 组合链路：`get(String,String...)` → 与 `Path.of(...)` 结果一致 → `getFileSystem` 为默认文件系统。

## 3. 开源代码实际用法调研（真实片段）
- `Path home = Paths.get(System.getProperty("java.home"));` — `jdk25u-dev/test/jdk/tools/sincechecker/SinceChecker.java:275`
- `Path p = Paths.get("");` — `jdk25u-dev/test/jdk/tools/lib/tests/JImageGenerator.java:181`
- `createJarFile(jarfile, dir, Paths.get("."));` — `jdk25u-dev/test/jdk/tools/lib/tests/JImageGenerator.java:289`

## 4. 测试用例设计 + 自动化实现（指向 PathsTest.java 黑盒 `testBlackBox`）
- 正常/组合: `get("a","b","c")` 与 `Path.of("a","b","c")` 结果等值；`get("a/b")` 按平台分隔符解析（用 `FileSystems.getDefault().getSeparator()` 拼接）；`get(URI)` 与 `toUri()` 往返。
- 边界: 空字符串 `get("")` 的 toString 为空（JDK 实际行为，见文件内修复）。
- 健壮性: 用默认文件系统分隔符构造断言，不硬编码 `/`；无全局副作用。
- 白盒 `testWhiteBox`: 反射校验 final class、非抽象、私有构造不可实例化、get(String,String...)/get(URI) 存在。
