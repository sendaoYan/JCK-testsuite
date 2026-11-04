# java.util.zip.ZipException 测试方案

白盒结构断言在 `ZipExceptionTest.testWhiteBox()`，黑盒行为断言在 `ZipExceptionTest.testBlackBox()`。

## 1. API 功能（按方法分组）

- **构造**：`ZipException()`、`ZipException(String s)`。
- 继承：`IOException`，表示 ZIP 格式/读取错误。

## 2. Java 用户如何使用

- 抛出侧：`new ZipFile(file)` 打开非 ZIP 字节、`GZIPInputStream` 魔数不符、ZIP 头损坏等。
- 捕获侧：`try { new ZipFile(f); } catch (ZipException e) { ... }`。
- 真实组合链路：写入非 ZIP 字节文件 → `new ZipFile(file)` → 抛 `ZipException`。

## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码，逐条标注来源。

- `src/java.base/share/classes/java/util/zip/ZipFile.java:1693`：`throw new ZipException("zip END header not found");` —— 非 ZIP 文件触发点。
- `src/java.base/share/classes/java/util/zip/GZIPInputStream.java:230-231`：`throw new ZipException("Not in GZIP format");` —— GZIP 魔数错误触发点。

## 4. 测试用例设计 + 自动化实现

- **正常**：无参 `getMessage()==null`、`getCause()==null`；`String` 构造保留 message；`instanceof IOException`。
- **组合**：真实触发 —— 写非 ZIP 字节文件后 `new ZipFile(file)` 抛 `ZipException` 且 message 非空。
- **自动化**：`ZipExceptionTest.testBlackBox()`；`testWhiteBox()` 校验 `getSuperclass()==IOException.class` 与两个构造器。
- **健壮性**：临时目录 `Files.createTempDirectory("jck")` 并在 `finally` 递归删除；headless、无 `@key`。
