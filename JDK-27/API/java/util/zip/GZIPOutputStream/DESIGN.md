# java.util.zip.GZIPOutputStream 测试方案

白盒结构断言在 `GZIPOutputStreamTest.testWhiteBox()`，黑盒行为断言在 `GZIPOutputStreamTest.testBlackBox()`。

## 1. API 功能（按方法分组）

- **构造**：`(OutputStream out)`、`(out, int size)`、`(out, boolean syncFlush)`、`(out, int size, boolean syncFlush)`（抛 `IOException`）。
- **写/收尾**：`write(byte[],int,int)`、`finish()`。
- 继承 `DeflaterOutputStream`，写 GZIP 流，头两字节为 `0x1f 0x8b`。

## 2. Java 用户如何使用

- 构造：`GZIPOutputStream gzout = new GZIPOutputStream(sink);`。
- 典型调用：`gzout.write(data); gzout.close();`（或 `finish()` 后 `close()`），读回用 `GZIPInputStream`。
- 真实组合链路：原始字节 → `GZIPOutputStream.write` → `close` → GZIP 字节 → `GZIPInputStream` 还原。

## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码与 `test/jdk`，逐条标注来源。

- `test/jdk/java/util/zip/GZIP/GZIPInZip.java:63`：`GZIPOutputStream gos1 = new GZIPOutputStream(zos);` —— 真实用法。
- `test/jdk/java/util/zip/GZIP/GZIPInputStreamRead.java:26`：测试 GZIPInput/OutputStream 的读写往返。
- `src/java.base/share/classes/java/util/zip/GZIPOutputStream.java:42`：`public class GZIPOutputStream extends DeflaterOutputStream`。

## 4. 测试用例设计 + 自动化实现

- **正常**：写入后前两字节为 `0x1f 0x8b`；经 `GZIPInputStream` 往返还原多字节 payload。
- **组合**：带 level 的构造器仍写合法 GZIP 头并能还原内容。
- **自动化**：`GZIPOutputStreamTest.testBlackBox()`；`testWhiteBox()` 校验 `extends DeflaterOutputStream`、4 个构造器与 `write/finish` 存在。
- **健壮性**：固定输入、ByteArray 流、headless、无 `@key`。
