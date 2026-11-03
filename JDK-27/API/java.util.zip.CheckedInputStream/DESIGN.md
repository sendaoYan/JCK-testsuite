# java.util.zip.CheckedInputStream 测试方案

白盒结构断言在 `CheckedInputStreamTest.testWhiteBox()`，黑盒行为断言在 `CheckedInputStreamTest.testBlackBox()`。

## 1. API 功能（按方法分组）

- **构造**：`CheckedInputStream(InputStream in, Checksum cksum)`。
- **读**：`read()`、`read(byte[] b, int off, int len)`、`skip(long n)`。
- **查询**：`getChecksum()`（返回构造时传入的同一 `Checksum` 实例）。

## 2. Java 用户如何使用

- 构造：`CheckedInputStream in = new CheckedInputStream(stream, new CRC32());`。
- 典型调用：读完后 `in.getChecksum().getValue()` 与直接计算的校验值对比；读数据与校验同步进行。
- 真实组合链路：`new CheckedInputStream(...)` → `read(...)` 循环 → `getChecksum().getValue()`。

## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码，逐条标注来源。

- `src/java.base/share/classes/java/util/zip/GZIPInputStream.java:204`：`CheckedInputStream in = new CheckedInputStream(this_in, crc);` —— GZIP 解压时边读边算 CRC。
- `src/java.base/share/classes/java/util/zip/CheckedInputStream.java:48`：构造器绑定 `InputStream + Checksum`。

## 4. 测试用例设计 + 自动化实现

- **正常**：读全量字节后内容与输入一致；`getChecksum()` 返回同一实例；校验值等于直接计算的 `CRC32`。
- **组合**：单字节 `read()` 循环与多字节 `read(byte[],off,len)` 循环都得到正确校验值；`read(1)+skip(4)+read(余量)` 的剩余字节数正确。
- **边界**：读尽后 `read()` 返回 -1。
- **自动化**：`CheckedInputStreamTest.testBlackBox()`；`testWhiteBox()` 校验 `extends FilterInputStream`、构造器与方法存在。
- **健壮性**：固定输入、ByteArray 流、headless、无 `@key`。
