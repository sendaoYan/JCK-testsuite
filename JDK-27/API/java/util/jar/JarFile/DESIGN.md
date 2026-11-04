# java.util.jar.JarFile 测试方案

白盒结构断言在 `JarFileTest.testWhiteBox()`，黑盒行为断言在 `JarFileTest.testBlackBox()`。本文件聚焦黑盒方案，自动化实现见 `JarFileTest.java` 黑盒部分。

## 1. API 功能（按方法分组）

- **静态**：`MANIFEST_NAME`（"META-INF/MANIFEST.MF"）、`baseVersion()`（feature 8）、`runtimeVersion()`（当前运行时 feature）。
- **构造**：`JarFile(String)`、`JarFile(String, boolean)`、`JarFile(File)`、`JarFile(File, boolean)`、`JarFile(File, boolean, int)`、`JarFile(File, boolean, int, Runtime.Version)`。
- **查询**：`getManifest()`、`getJarEntry(String)`、`getEntry(String)`、`entries()`、`stream()`、`versionedStream()`、`getVersion()`、`isMultiRelease()`、`getInputStream(ZipEntry)`、`size()`（继承）。

## 2. Java 用户如何使用

- 构造：`new JarFile(file)`；多版本用 `new JarFile(file, false, ZipFile.OPEN_READ, JarFile.runtimeVersion())`。
- 典型调用：`jf.getManifest().getMainAttributes().getValue(Name.MANIFEST_VERSION)`；`jf.getJarEntry(name)`；`jf.getInputStream(entry)`；`jf.entries()`/`jf.stream()` 遍历。
- 真实组合链路：`JarOutputStream` 写 jar → `JarFile` 读 → `getManifest → getJarEntry → getInputStream → 内容比对`。

## 3. 开源代码实际用法调研

> 本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev` 与自带测试 `test/jdk`，逐条标注来源，未凭记忆编造。

- `test/jdk/java/util/jar/JarFile/VerifySignedJar.java:61,74,106-107`：`try (JarFile jf = new JarFile(s.toFile())) {...}`、`jf.getJarEntry("getprop.class")`、`JarOutputStream ... putNextEntry(new JarEntry(...))` —— 打开/查询/写入典型链。
- `test/jdk/tools/jar/multiRelease/Basic.java:514-518`：`try (JarFile jf = new JarFile(new File(jarfile), true, ...)) {... getMainAttributes()}` —— 多版本 jar 打开与主属性读取。
- `test/jdk/java/util/jar/JarFile/mrjar/MultiReleaseJarProperties.java:126,152`：`new JarFile(multirelease)`、`jf.getJarEntry(entryName)` —— 多版本条目解析。
- `src/java.base/share/classes/java/util/jar/JarFile.java:332-353`：构造器将 version 规范化 —— 默认构造用 `BASE_VERSION` 的来源。

## 4. 测试用例设计 + 自动化实现

- **普通 jar（临时目录文件）**：`size()==3`；`getManifest` 主属性/段属性；`getJarEntry`/`getEntry`/未知条目 null；`entries()` 排序断言；`stream()`/`versionedStream()` 与 entries 一致；`getInputStream` 内容逐字节相等。
- **版本/常量**：`MANIFEST_NAME` 值；`isMultiRelease` false；`getVersion`==baseVersion（feature 8）；`baseVersion/runtimeVersion` feature。
- **多版本 jar**：默认构造按 base 版本读 base 内容；`runtimeVersion()` 构造解析 `META-INF/versions/9/version.txt`（`getName` 为基名、`getRealName` 为原始路径、内容 v9）；`versionedStream()` 隐藏原始版本化路径；`baseVersion()` 构造读 base。
- **异常/关闭**：`getJarEntry(null)` 抛 `NullPointerException`；不存在条目 `getInputStream` 返回 null；close 后 `size`/`getEntry`/`getInputStream` 抛 `IllegalStateException`。
- **自动化**：`JarFileTest.testBlackBox()` 内 `testSimpleJar/testMultiReleaseJar`，`Files.createTempDirectory("jck")` + `finally` 递归清理。
- **健壮性**：固定 UTF-8、排序后再断言枚举顺序、固定输入、无全局状态/网络/线程/端口。
