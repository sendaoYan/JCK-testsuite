# java.util.zip.ZipFile 测试方案

白盒结构断言在 `ZipFileTest.testWhiteBox()`，黑盒行为断言在 `ZipFileTest.testBlackBox()`。

## 1. API 功能（按方法分组）

- **常量**：`OPEN_READ=1`、`OPEN_DELETE=4`。
- **构造**：`(String)`、`(File)`、`(File,int)`、`(File,Charset)`、`(String,Charset)`（抛 `IOException`/`ZipException`）。
- **查询**：`getEntry(String)`、`getInputStream(ZipEntry)`、`size()`、`entries()`、`stream()`、`getComment()`、`getName()`、`toString()`。
- **关闭**：`close()`（实现 `Closeable`）。

## 2. Java 用户如何使用

- 构造：`ZipFile zf = new ZipFile(file);`（文件需是合法 ZIP）。
- 典型调用：`zf.getEntry(name)` 取条目 → `zf.getInputStream(entry)` 读内容；`zf.entries()` 遍历；`zf.stream()` 流式处理；用完 `zf.close()`。
- 真实组合链路：写 ZIP 文件 → `new ZipFile` → `getEntry` → `getInputStream` → 读内容 → `close`。

## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码与 `test/jdk`，逐条标注来源。

- `test/jdk/java/util/zip/ZipFile/TestZipFile.java:165`：`zf.stream().map(e -> e.getName()).toArray(...)` —— `stream()` 用法。
- `test/jdk/java/util/zip/ZipFile/TestZipFile.java:180`：`try (InputStream is = zf.getInputStream(e))` —— 读条目。
- `test/jdk/java/util/zip/ZipFile/Assortment.java:282`：`Enumeration<? extends ZipEntry> en = f.entries();` —— `entries()` 遍历。
- `src/java.base/share/classes/java/util/zip/ZipFile.java:532/585/681`：`entries()`/`stream()`/`size()` 实现点。

## 4. 测试用例设计 + 自动化实现

- **正常**：`size()` 正确；`getComment()` 回读写时注释；`getName()` 结尾为文件名；`getEntry` 命中/缺失；`getInputStream` 读回内容。
- **组合**：`entries()` 枚举名收集后排序断言；`stream()` 流名收集后排序断言（ZIP 条目顺序不保证，需排序）。
- **自动化**：`ZipFileTest.testBlackBox()`；`testWhiteBox()` 校验 `implements Closeable`、常量、构造器与方法存在。
- **健壮性**：临时目录 + `finally` 递归删除；排序后再断言无序枚举；headless、无 `@key`。
