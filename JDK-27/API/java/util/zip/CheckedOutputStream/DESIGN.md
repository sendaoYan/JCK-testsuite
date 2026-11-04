# java.util.zip.CheckedOutputStream 测试方案

白盒结构断言在 `CheckedOutputStreamTest.testWhiteBox()`，黑盒行为断言在 `CheckedOutputStreamTest.testBlackBox()`。

## 1. API 功能（按方法分组）

- **构造**：`CheckedOutputStream(OutputStream out, Checksum cksum)`。
- **写**：`write(int b)`、`write(byte[] b, int off, int len)`。
- **查询**：`getChecksum()`（返回构造时传入的同一 `Checksum` 实例）。

## 2. Java 用户如何使用

- 构造：`CheckedOutputStream out = new CheckedOutputStream(sink, new Adler32());`。
- 典型调用：写入数据后 `out.getChecksum().getValue()` 得到与写入数据一致的校验值；写数据与校验同步。
- 真实组合链路：`new CheckedOutputStream(...)` → `write(...)` → `flush()` → `getChecksum().getValue()`。

## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码与 `test/jdk`，逐条标注来源。

- `test/jdk/tools/launcher/ZipMeUp.java:45`：`CheckedOutputStream cos = new CheckedOutputStream(baos, crc);` —— 官方测试写字节流并计算 CRC。
- `src/java.base/share/classes/java/util/zip/CheckedOutputStream.java:49`：构造器绑定 `OutputStream + Checksum`。

## 4. 测试用例设计 + 自动化实现

- **正常**：写入后底层流内容与输入一致；`getChecksum()` 返回同一实例；校验值等于直接计算的 `Adler32`。
- **组合**：多字节 `write(byte[],off,len)` 与单字节 `write(int)` 循环都得到正确校验值。
- **自动化**：`CheckedOutputStreamTest.testBlackBox()`；`testWhiteBox()` 校验 `extends FilterOutputStream`、构造器与方法存在。
- **健壮性**：固定输入、ByteArray 流、headless、无 `@key`。
