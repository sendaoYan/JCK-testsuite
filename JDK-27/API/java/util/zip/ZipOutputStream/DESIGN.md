# java.util.zip.ZipOutputStream 测试方案

白盒结构断言在 `ZipOutputStreamTest.testWhiteBox()`，黑盒行为断言在 `ZipOutputStreamTest.testBlackBox()`。

## 1. API 功能（按方法分组）

- **常量**：`STORED=0`、`DEFLATED=8`。
- **构造**：`(OutputStream out)`、`(OutputStream out, Charset charset)`。
- **配置**：`setComment(String)`、`setMethod(int)`、`setLevel(int)`。
- **条目/写**：`putNextEntry(ZipEntry)`、`closeEntry()`、`write(byte[],int,int)`、`finish()`、`close()`。
- 继承 `DeflaterOutputStream implements ZipConstants`。

## 2. Java 用户如何使用

- 构造：`ZipOutputStream zout = new ZipOutputStream(sink);`。
- 典型调用：`putNextEntry(entry)` → `write(bytes)` → `closeEntry()`；STORED 条目需先 `setMethod(STORED)+setSize(n)+setCrc(crc)`。
- 真实组合链路：`setMethod/setLevel/setComment` → 循环 `putNextEntry/write/closeEntry` → `finish`/`close` → 读回验证。

## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码与 `test/jdk`，逐条标注来源。

- `test/jdk/java/util/zip/ZipOutputStream/EmptyComment.java:86-90`：`ZipOutputStream zos = new ZipOutputStream(baos); zos.putNextEntry(new ZipEntry("x"));` —— 写条目。
- `src/java.base/share/classes/java/util/jar/JarOutputStream.java:46`：`public class JarOutputStream extends ZipOutputStream` —— 关键子类。
- `src/java.base/share/classes/java/util/zip/ZipOutputStream.java:202-204`：STORED 条目前置 `setMethod/setSize/setCrc` 说明。

## 4. 测试用例设计 + 自动化实现

- **正常**：写 STORED（预计算 size+crc）与 DEFLATED 多条目到临时文件，读回断言条目顺序与内容。
- **组合**：`setMethod(DEFLATED)+setLevel+setComment`；`finish()` 后文件非空；`ZipInputStream` 读回逐条目还原。
- **自动化**：`ZipOutputStreamTest.testBlackBox()`；`testWhiteBox()` 校验 `extends DeflaterOutputStream`、常量、构造器与方法存在。
- **健壮性**：临时目录 + `finally` 递归删除；固定内容、headless、无 `@key`。
