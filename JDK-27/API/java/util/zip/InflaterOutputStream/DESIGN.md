# java.util.zip.InflaterOutputStream 测试方案

白盒结构断言在 `InflaterOutputStreamTest.testWhiteBox()`，黑盒行为断言在 `InflaterOutputStreamTest.testBlackBox()`。

## 1. API 功能（按方法分组）

- **构造**：`(OutputStream out)`、`(OutputStream out, Inflater infl)`、`(OutputStream out, Inflater infl, int bufLen)`。
- **写/收尾**：`write(int)`、`write(byte[],int,int)`、`flush()`、`finish()`、`close()`。

## 2. Java 用户如何使用

- 构造：`InflaterOutputStream ios = new InflaterOutputStream(sink);`。
- 典型调用：`ios.write(compressedBytes); ios.finish(); ios.close();`，底层流得到解压数据。
- 真实组合链路：压缩字节 → `InflaterOutputStream.write` → `finish` → 原始字节。

## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码与 `test/jdk`，逐条标注来源。

- `test/jdk/java/util/zip/DeflateIn_InflateOut.java:57`：`ios = new InflaterOutputStream(baos);` —— 单参构造。
- `test/jdk/java/util/zip/DeflateIn_InflateOut.java:60-61`：`Inflater inf = new Inflater(); ios = new InflaterOutputStream(baos, inf);` —— 传入 `Inflater`。

## 4. 测试用例设计 + 自动化实现

- **正常**：写入压缩字节 + `finish` + `close` 后还原内容。
- **组合**：单字节 `write(int)` 循环与多字节 `write(byte[],int,int)` 都正确还原。
- **自动化**：`InflaterOutputStreamTest.testBlackBox()`；`testWhiteBox()` 校验 `extends FilterOutputStream`、三个构造器与写/收尾方法存在。
- **健壮性**：固定输入、ByteArray 流、headless、无 `@key`。
