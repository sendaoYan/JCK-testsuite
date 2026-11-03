# java.util.zip.ZipInputStream 测试方案

白盒结构断言在 `ZipInputStreamTest.testWhiteBox()`，黑盒行为断言在 `ZipInputStreamTest.testBlackBox()`。

## 1. API 功能（按方法分组）

- **构造**：`(InputStream in)`、`(InputStream in, Charset charset)`。
- **条目**：`getNextEntry()`、`closeEntry()`。
- **读**：`read()`、`read(byte[],int,int)`、`readAllBytes()`、`readNBytes(int)`、`readNBytes(byte[],int,int)`、`skip(long)`、`skipNBytes(long)`、`transferTo(OutputStream)`、`available()`。
- 继承 `InflaterInputStream implements ZipConstants`。

## 2. Java 用户如何使用

- 构造：`ZipInputStream zin = new ZipInputStream(in);`。
- 典型调用：循环 `while ((e = zin.getNextEntry()) != null) { zin.readAllBytes(); zin.closeEntry(); }`。
- 真实组合链路：`getNextEntry` → `read/readAllBytes` → `closeEntry` → 下一 `getNextEntry`。

## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码与 `test/jdk`，逐条标注来源。

- `test/jdk/java/util/zip/ZipInputStream/Zip64DataDescriptor.java:267`：`try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip)))` —— 读回 ZIP。
- `test/jdk/java/util/zip/ZipInputStream/Zip64DataDescriptor.java:118-121`：`ZipOutputStream` 侧 `putNextEntry(ze)` —— 写读配合。
- `src/java.base/share/classes/java/util/zip/ZipInputStream.java:238`：`readAllBytes()` 读取当前条目剩余字节的实现点。

## 4. 测试用例设计 + 自动化实现

- **正常**：多条目按写入顺序读出；`readAllBytes()` 读当前条目内容；`read(byte[],off,len)` 读回长度与内容；读尽后 `getNextEntry()` 返回 null。
- **组合**：`closeEntry()` 后推进下一 `getNextEntry()`；`readNBytes(int)` 读固定字节。
- **自动化**：`ZipInputStreamTest.testBlackBox()`；`testWhiteBox()` 校验 `extends InflaterInputStream`、构造器与条目/读方法存在。
- **健壮性**：固定输入、ByteArray 流、headless、无 `@key`。
