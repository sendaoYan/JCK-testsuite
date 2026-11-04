# java.util.jar.JarInputStream 测试方案

白盒结构断言在 `JarInputStreamTest.testWhiteBox()`，黑盒行为断言在 `JarInputStreamTest.testBlackBox()`。本文件聚焦黑盒方案，自动化实现见 `JarInputStreamTest.java` 黑盒部分。

## 1. API 功能（按方法分组）

- **构造**：`JarInputStream(InputStream)`、`JarInputStream(InputStream, boolean verify)`；构造时若首条为 manifest（或 `META-INF/` + manifest）则解析。
- **查询**：`getManifest()`；`getNextJarEntry()`（返回 `JarEntry`，跳过 manifest）；`getNextEntry()`（返回 `ZipEntry`，跳过 manifest）。
- **读数据**：`read()`/`read(byte[], int, int)`（继承 `ZipInputStream`），当前条目读至末尾。

## 2. Java 用户如何使用

- 构造：`new JarInputStream(inputStream)`；`new JarInputStream(in, true)`。
- 典型调用：`jin.getManifest()`；`while ((e = jin.getNextJarEntry()) != null) { read... }`。
- 真实组合链路：`JarOutputStream` 写字节 → `JarInputStream` 读 → `getManifest → getNextJarEntry → read 内容`。

## 3. 开源代码实际用法调研

> 本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev` 与自带测试 `test/jdk`，逐条标注来源，未凭记忆编造。

- `test/jdk/java/util/jar/JarInputStream/ExtraFileInMetaInf.java:60`：`new JarInputStream(new FileInputStream("x.jar"))` —— try-with-resources 打开。
- `test/jdk/java/util/jar/JarInputStream/EmptyJar.java:37`：`JarInputStream is = new JarInputStream(...)` —— 空 jar 打开。
- `src/java.base/share/classes/java/util/jar/JarInputStream.java:128-142`：构造读取并跳过首条 manifest —— 行为来源。

## 4. 测试用例设计 + 自动化实现

- **有 manifest**：`getManifest` 非空且版本/主类正确；`getNextJarEntry` 循环返回 `a.txt`/`b.txt` 且跳过 manifest，内容逐条比对。
- **getNextEntry**：跳过 manifest 返回首条 `ZipEntry`，`read(byte[],int,int)` 读取内容。
- **无 manifest**：`getManifest` 为 null，`getNextJarEntry` 返回唯一条目，读完返回 null。
- **空 jar**：`getManifest` null、`getNextJarEntry` null。
- **自动化**：`JarInputStreamTest.testBlackBox()` 内 `testWithManifest/testGetNextEntry/testWithoutManifest/testEmptyJar`。
- **健壮性**：固定 UTF-8、字节级内容比对、无墙钟/随机、无全局状态/文件/网络/线程。
