# java.util.jar.JarFile 测试方案

白盒结构断言在 `JarFileTest.testWhiteBox()`，黑盒行为断言在 `JarFileTest.testSimpleJar()`/`testCtor()`。

## 1. API 功能（按方法分组）

- **常量**：`MANIFEST_NAME`（"META-INF/MANIFEST.MF"）。
- **构造**：`JarFile(String)`、`JarFile(String, boolean)`、`JarFile(File)`、`JarFile(File, boolean)`、`JarFile(File, boolean, int)`。
- **查询**：`getManifest()`、`getJarEntry(String)`、`getEntry(String)`、`entries()`、`stream()`、`getInputStream(ZipEntry)`、`size()`（继承）。

## 2. Java 用户如何使用

- 构造：`new JarFile(file)`。
- 典型调用：`jf.getManifest().getMainAttributes().getValue(Name.MANIFEST_VERSION)`；`jf.getJarEntry(name)`；`jf.getInputStream(entry)`；`jf.entries()`/`jf.stream()` 遍历。
- 真实组合链路：`JarOutputStream` 写 jar → `JarFile` 读 → `getManifest → getJarEntry → getInputStream → 内容比对`。

## 3. 参考来源

- OpenJDK 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/util/jar/JarFile.java`（JDK-8 无多版本/versionedStream/getVersion）。

## 4. 测试用例设计

- **普通 jar（临时目录文件）**：`size()==3`；`getManifest` 主属性/段属性；`getJarEntry`/`getEntry`/未知条目 null；`entries()` 排序断言；`stream()` 与 entries 一致；`getInputStream` 内容逐字节相等。
- **异常/关闭**：`getJarEntry(null)` 抛 `NullPointerException`；不存在条目 `getInputStream` 返回 null；close 后 `size`/`getEntry`/`getInputStream` 抛 `IllegalStateException`。
- **构造**：null String/File → NPE；缺失文件 → `FileNotFoundException`。
- **健壮性**：固定 UTF-8、排序后断言枚举顺序、临时目录 + finally 清理。
