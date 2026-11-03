# java.util.zip.GZIPInputStream 测试方案

白盒结构断言在 `GZIPInputStreamTest.testWhiteBox()`，黑盒行为断言在 `GZIPInputStreamTest.testBlackBox()`。

## 1. API 功能（按方法分组）

- **常量**：`GZIP_MAGIC = 0x8b1f`。
- **构造**：`(InputStream in)`、`(InputStream in, int size)`（抛 `IOException`）。
- **读/收尾**：`read(byte[],int,int)`、`close()`。
- 继承 `InflaterInputStream`，解压 GZIP 流（含 1f 8b 魔数头、方法/标志/MTIME/OS 等头字段）。

## 2. Java 用户如何使用

- 构造：`GZIPInputStream gzin = new GZIPInputStream(in);`。
- 典型调用：`byte[] data = gzin.readAllBytes();` 或循环 `read`，得到解压内容；`close()`。
- 真实组合链路：`GZIPOutputStream` 写 → `GZIPInputStream` 读回还原。

## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码与 `test/jdk`，逐条标注来源。

- `test/jdk/java/util/zip/GZIP/GZIPInZip.java:63`：`GZIPOutputStream gos1 = new GZIPOutputStream(zos);` —— GZIP 流嵌套进 ZIP 条目。
- `test/jdk/java/util/zip/GZIP/BasicGZIPInputStreamTest.java:44-45`：`new GZIPInputStream(null)` / `(null, 1)` —— 构造器 NPE 契约测试。
- `src/java.base/share/classes/java/util/zip/GZIPInputStream.java:183`：`public static final int GZIP_MAGIC = 0x8b1f;`。
- `src/java.base/share/classes/java/util/zip/GZIPInputStream.java:230-231`：魔数不符抛 `ZipException("Not in GZIP format")`。

## 4. 测试用例设计 + 自动化实现

- **正常**：多字节 payload 经 `GZIPOutputStream` 写后读回内容一致；`GZIP_MAGIC == 0x8b1f`。
- **组合**：`read(byte[],off,len)` 先读前缀再读余量拼接正确。
- **边界**：非 GZIP 字节构造时抛 `ZipException`。
- **自动化**：`GZIPInputStreamTest.testBlackBox()`；`testWhiteBox()` 校验 `extends InflaterInputStream`、`GZIP_MAGIC`、构造器与方法存在。
- **健壮性**：固定输入、ByteArray 流、headless、无 `@key`。
