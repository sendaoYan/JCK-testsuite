# java.util.jar.JarOutputStream 测试方案

白盒结构断言在 `JarOutputStreamTest.testWhiteBox()`，黑盒行为断言在 `JarOutputStreamTest.testBlackBox()`。本文件聚焦黑盒方案，自动化实现见 `JarOutputStreamTest.java` 黑盒部分。

## 1. API 功能（按方法分组）

- **构造**：`JarOutputStream(OutputStream)`、`JarOutputStream(OutputStream, Manifest)`；带 manifest 时立即写 `META-INF/MANIFEST.MF` 为首条目。
- **写条目**：`putNextEntry(ZipEntry)`、`write(...)`、`closeEntry()`（继承 `ZipOutputStream`），首条目自动加 JAR magic extra field。

## 2. Java 用户如何使用

- 构造：`new JarOutputStream(out)`；`new JarOutputStream(out, manifest)`。
- 典型调用：`putNextEntry(new JarEntry("a.txt")) → write(bytes) → closeEntry()`；`finish()`/`close()`。
- 真实组合链路：`Manifest` + `JarOutputStream` 写文件 → `JarInputStream`/`JarFile` 读回 → manifest 首条目、条目内容字节级比对。

## 3. 开源代码实际用法调研

> 本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev` 与自带测试 `test/jdk`，逐条标注来源，未凭记忆编造。

- `test/jdk/java/util/jar/JarFile/VerifySignedJar.java:106-107`：`try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(j))){ out.putNextEntry(new JarEntry("getprop.class")); ... }` —— 写条目典型链。
- `src/java.base/share/classes/java/util/jar/JarOutputStream.java:59-63`：带 manifest 构造写 `ZipEntry(JarFile.MANIFEST_NAME)` 为首条目 —— 首条目来源。

## 4. 测试用例设计 + 自动化实现

- **首条目 manifest**：`JarOutputStream(out, manifest)` 写后 `JarInputStream.getManifest()` 非空且版本/主类正确；`JarOutputStream(out, null manifest)` 抛 `NullPointerException`。
- **无 manifest**：`JarOutputStream(out)` 写后 `getManifest()` 为 null。
- **STORED + 预计算 size/crc**：`ZipEntry.STORED` + `setSize/setCompressedSize/setCrc(CRC32)` 写入，读回 size/crc/内容字节级相等。
- **多条目往返（临时目录文件）**：deflated + stored 两条目，`JarFile` 读回内容逐字节相等，`JarInputStream` 读回顺序为 deflated 后 stored。
- **自动化**：`JarOutputStreamTest.testBlackBox()` 内 `testManifestFirstEntry/testNoManifest/testStoredEntryAndRoundTrip`；`Files.createTempDirectory("jck")` + `finally` 递归清理。
- **健壮性**：固定 UTF-8、固定 CRC32 输入、无墙钟/随机、无全局状态/网络/线程。
