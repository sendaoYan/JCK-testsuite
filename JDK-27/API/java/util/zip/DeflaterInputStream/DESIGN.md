# java.util.zip.DeflaterInputStream 测试方案

白盒结构断言在 `DeflaterInputStreamTest.testWhiteBox()`，黑盒行为断言在 `DeflaterInputStreamTest.testBlackBox()`。

## 1. API 功能（按方法分组）

- **构造**：`DeflaterInputStream(InputStream in)`、`(InputStream in, Deflater defl)`、`(InputStream in, Deflater defl, int bufLen)`。
- **读/查询**：`read()`、`read(byte[],int,int)`、`skip(long)`、`available()`、`close()`。
- **mark/reset**：`markSupported()`（恒 false）、`mark(int)`、`reset()`（抛 `IOException`）。

## 2. Java 用户如何使用

- 构造：`DeflaterInputStream dis = new DeflaterInputStream(raw);`。
- 典型调用：读出的字节是压缩数据，再交给 `InflaterInputStream` 解压还原。
- 真实组合链路：原始字节 → `DeflaterInputStream.read` 循环收压缩字节 → `InflaterInputStream` 还原。

## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码与 `test/jdk`，逐条标注来源。

- `test/jdk/java/util/zip/DeflateIn_InflateOut.java:48`：`dis = new DeflaterInputStream(bais);` —— 单参构造。
- `test/jdk/java/util/zip/DeflateIn_InflateOut.java:52`：`dis = new DeflaterInputStream(bais, def);` —— 传入 `Deflater`。
- `src/java.base/share/classes/java/util/zip/DeflaterInputStream.java:307`：`markSupported()` 返回 false 的实现点。

## 4. 测试用例设计 + 自动化实现

- **正常**：读压缩字节产出非空；经 `InflaterInputStream` 往返还原内容。
- **组合**：`markSupported()` false；`mark` 后 `reset()` 抛 `IOException`；`close()` 正常。
- **自动化**：`DeflaterInputStreamTest.testBlackBox()`；`testWhiteBox()` 校验 `extends FilterInputStream`、三个构造器与流方法存在。
- **健壮性**：固定输入、ByteArray 流、headless、无 `@key`。
