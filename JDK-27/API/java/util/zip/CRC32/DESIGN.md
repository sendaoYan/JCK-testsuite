# java.util.zip.CRC32 测试方案

白盒结构断言在 `CRC32Test.testWhiteBox()`，黑盒行为断言在 `CRC32Test.testBlackBox()`。

## 1. API 功能（按方法分组）

- **构造**：`CRC32()`。
- **更新**：`update(int b)`、`update(byte[] b, int off, int len)`、`update(ByteBuffer buffer)`。
- **查询/重置**：`getValue()`、`reset()`。
- 语义：CRC-32 校验和，初始值 0，可增量累积，`reset()` 回到 0。

## 2. Java 用户如何使用

- 构造：`CRC32 c = new CRC32();`（初始 0）。
- 典型调用：`c.update(bytes); long v = c.getValue();`；流式处理时配合 `CheckedInputStream`/`CheckedOutputStream`。
- 真实组合链路：`new CRC32()` → `update(...)` → `getValue()`；`reset()` 后复用。

## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码与 `test/jdk`，逐条标注来源。

- `src/java.base/share/classes/java/util/zip/ZipOutputStream.java:74`：`private CRC32 crc = new CRC32();` —— 写 ZIP 条目时计算 CRC。
- `src/java.base/share/classes/java/util/zip/GZIPInputStream.java:49`：`protected CRC32 crc = new CRC32();` —— GZIP 解压校验。
- `src/java.base/share/classes/jdk/internal/loader/URLClassPath.java:731`：`CRC32 crc32 = new CRC32();` —— 类加载缓存校验。
- `test/jdk/java/util/zip/TestCRC32.java:37`：`ChecksumBase.testAll(new CRC32(), 0xCBF43926L);` —— 官方校验值 0xCBF43926。

## 4. 测试用例设计 + 自动化实现

- **正常**：初始 0；`update(byte[],off,len)` 对 "123456789" 得到 `0xCBF43926`；`update(int)`、`update(ByteBuffer)` 同值；`reset()` 回到 0。
- **组合**：增量更新（前 4 + 后 5 字节）等价于整块更新。
- **边界**：空输入仍为 0。
- **自动化**：`CRC32Test.testBlackBox()` 实现；`testWhiteBox()` 校验 `implements Checksum`、非 final、方法存在。
- **健壮性**：固定输入、headless、无 `@key`。
