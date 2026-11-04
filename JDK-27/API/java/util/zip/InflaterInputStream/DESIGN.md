# java.util.zip.InflaterInputStream 测试方案

白盒结构断言在 `InflaterInputStreamTest.testWhiteBox()`，黑盒行为断言在 `InflaterInputStreamTest.testBlackBox()`。

## 1. API 功能（按方法分组）

- **构造**：`(InputStream in)`、`(InputStream in, Inflater inf)`、`(InputStream in, Inflater inf, int size)`。
- **读/查询**：`read()`、`read(byte[],int,int)`、`available()`、`skip(long)`、`close()`。
- **mark/reset**：`markSupported()`（false）、`mark(int)`、`reset()`（抛 `IOException`）。

## 2. Java 用户如何使用

- 构造：`InflaterInputStream iis = new InflaterInputStream(compressed);`。
- 典型调用：读出的字节是解压后的数据；配合 `DeflaterOutputStream`/`DeflaterInputStream` 往返。
- 真实组合链路：压缩字节 → `InflaterInputStream.read` 循环 → 原始字节。

## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码，逐条标注来源。

- `src/java.base/share/classes/java/util/zip/ZipInputStream.java:78`：`public class ZipInputStream extends InflaterInputStream ...` —— 关键子类。
- `src/java.base/share/classes/java/util/zip/GZIPInputStream.java`：`extends InflaterInputStream` —— GZIP 解压基于它。
- `src/java.base/share/classes/java/util/zip/InflaterInputStream.java:327`：`markSupported()` 返回 false 的实现点。

## 4. 测试用例设计 + 自动化实现

- **正常**：读回解压长度与内容一致；单字节 `read()` 循环计数正确。
- **组合**：`markSupported()` false；`mark` 后 `reset()` 抛 `IOException`；`close()`。
- **自动化**：`InflaterInputStreamTest.testBlackBox()`；`testWhiteBox()` 校验 `extends FilterInputStream`、三个构造器与流方法存在。
- **健壮性**：固定输入、ByteArray 流、headless、无 `@key`。
