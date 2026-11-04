# java.util.jar.JarEntry 测试方案

白盒结构断言在 `JarEntryTest.testWhiteBox()`，黑盒行为断言在 `JarEntryTest.testBlackBox()`。本文件聚焦黑盒方案，自动化实现见 `JarEntryTest.java` 黑盒部分。

## 1. API 功能（按方法分组）

- **构造**：`JarEntry(String name)`、`JarEntry(ZipEntry ze)`、`JarEntry(JarEntry je)`（拷贝，含 `attr/certs/signers`）。
- **查询**：`getAttributes()`（`throws IOException`，独立条目为 null）、`getCertificates()`、`getCodeSigners()`。
- **继承自 ZipEntry**：`getName/isDirectory/getTime/setTime/getSize/setSize/getComment/setComment/getMethod/setMethod/getCompressedSize/setCompressedSize/getCrc/setCrc` 等。

## 2. Java 用户如何使用

- 构造：`new JarEntry("file.txt")`；`new JarEntry(zipEntry)`；`new JarEntry(jarEntry)`。
- 典型调用：`e.getName()`、`e.isDirectory()`、`e.setSize(100)`；证书/签名者未签名为 `null`。
- 真实组合链路：`new JarEntry("dir/").isDirectory() == true`；`new JarEntry(zipEntry)` 继承 name/comment/time/size。

## 3. 开源代码实际用法调研

> 本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev` 与自带测试 `test/jdk`，逐条标注来源，未凭记忆编造。

- `test/jdk/java/util/jar/JarFile/VerifySignedJar.java:107`：`out.putNextEntry(new JarEntry("getprop.class"));` —— 写 jar 用 `JarEntry(String)`。
- `src/java.base/share/classes/java/util/jar/JarFile.java:555`：`...map(this::getJarEntry)` —— 多版本流按 `getJarEntry` 解析。
- `src/java.base/share/classes/java/util/jar/JarEntry.java:90-92`：拷贝构造复制 `attr/certs/signers` —— 拷贝语义来源。

## 4. 测试用例设计 + 自动化实现

- **正常**：`getName`、目录条目 `isDirectory`；`getCertificates`/`getCodeSigners`/`getAttributes` 独立条目为 null。
- **继承**：`setSize/getSize`、`setCompressedSize/getCompressedSize`、`setMethod/getMethod`（STORED）、`setComment/getComment`。
- **拷贝**：`JarEntry(ZipEntry)` 继承 name/comment/time（固定偶数秒 epoch）/size；`JarEntry(JarEntry)` 继承 name/size/comment 且 attrs/certs/signers 为 null。
- **边界/异常**：`new JarEntry((String) null)` 抛 `NullPointerException`。
- **跨类组合**：`JarEntry.getAttributes()` 在 `JarFile` 打开的真实条目上返回 manifest 段属性（见 `JarFileTest`）。
- **自动化**：`JarEntryTest.testBlackBox()` 内 `testBasics/testAttributes/testZipEntryCopy/testJarEntryCopy/testInheritedSetters/testNullName`。
- **健壮性**：固定 epoch、无墙钟/随机、无全局状态/文件/网络/线程。
