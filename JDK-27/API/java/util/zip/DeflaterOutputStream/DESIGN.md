# java.util.zip.DeflaterOutputStream 测试方案

白盒结构断言在 `DeflaterOutputStreamTest.testWhiteBox()`，黑盒行为断言在 `DeflaterOutputStreamTest.testBlackBox()`。

## 1. API 功能（按方法分组）

- **构造**：`(OutputStream out)`、`(out, Deflater def)`、`(out, Deflater def, int size)`、`(out, Deflater def, boolean syncFlush)`、`(out, Deflater def, int size, boolean syncFlush)`、`(out, boolean syncFlush)`。
- **写/收尾**：`write(int)`、`write(byte[],int,int)`、`flush()`、`finish()`、`close()`。

## 2. Java 用户如何使用

- 构造：`DeflaterOutputStream dos = new DeflaterOutputStream(sink);`。
- 典型调用：`dos.write(data); dos.finish(); dos.close();`，底层流得到压缩字节，再经 `InflaterInputStream` 还原。
- 真实组合链路：原始字节 → `DeflaterOutputStream.write` → `finish` → 底层压缩字节 → `InflaterInputStream` 还原。

## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码，逐条标注来源。

- `src/java.base/share/classes/java/util/zip/GZIPOutputStream.java:42`：`public class GZIPOutputStream extends DeflaterOutputStream` —— 关键子类，GZIP 基于它。
- `src/java.base/share/classes/java/util/zip/ZipOutputStream.java`：`extends DeflaterOutputStream` —— ZIP 压缩条目基于它。

## 4. 测试用例设计 + 自动化实现

- **正常**：写入+`finish`+`close` 产出压缩字节；经 `InflaterInputStream` 往返还原内容。
- **组合**：单字节 `write(int)` 循环与多字节 `write(byte[],int,int)` 都正确往返；自定义 `Deflater`+缓冲区大小的构造器。
- **自动化**：`DeflaterOutputStreamTest.testBlackBox()`；`testWhiteBox()` 校验 `extends FilterOutputStream`、6 个构造器与写/收尾方法存在。
- **健壮性**：固定输入、ByteArray 流、headless、无 `@key`。
