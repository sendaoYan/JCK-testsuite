# java.util.zip.ZipEntry 测试方案

白盒结构断言在 `ZipEntryTest.testWhiteBox()`，黑盒行为断言在 `ZipEntryTest.testBlackBox()`。

## 1. API 功能（按方法分组）

- **常量**：`STORED=0`、`DEFLATED=8`。
- **构造**：`(String name)`、`(ZipEntry e)` 拷贝构造。
- **名称/目录**：`getName()`（名称不可变，无 `setName`）、`isDirectory()`。
- **时间**：`setTime(long)`/`getTime()`、`setTimeLocal(LocalDateTime)`/`getTimeLocal()`、`setLastModifiedTime/getLastModifiedTime`、`setLastAccessTime/getLastAccessTime`、`setCreationTime/getCreationTime`（`FileTime` 访问器返回 `this`）。
- **大小/校验/方法**：`setSize/getSize`、`setCompressedSize/getCompressedSize`、`setCrc/getCrc`、`setMethod/getMethod`。
- **额外/注释/杂项**：`setExtra/getExtra`、`setComment/getComment`、`toString`、`hashCode`（基于 name）、`clone`。

## 2. Java 用户如何使用

- 构造：`ZipEntry e = new ZipEntry("dir/file.txt");`；写入 STORED 条目需 `setMethod(STORED)+setSize(n)+setCrc(crc)`。
- 典型调用：`e.setSize(100); e.setCrc(12345L); e.setMethod(DEFLATED); e.setTime(epochMillis);`。
- 真实组合链路：`new ZipEntry(name)` → `setMethod/setSize/setCrc/setTime` → `ZipOutputStream.putNextEntry(e)` → 写数据 → `closeEntry()`。

## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码与 `test/jdk`，逐条标注来源。

- `src/java.base/share/classes/java/util/zip/ZipOutputStream.java:202-204`：`e.setMethod(ZipEntry.STORED); e.setSize(0); e.setCrc(0);` —— STORED 条目前置设置。
- `src/java.base/share/classes/java/util/jar/JarOutputStream.java:64`：`ZipEntry e = new ZipEntry(JarFile.MANIFEST_NAME);` —— 真实自用。
- `test/jdk/java/util/zip/TestLocalTime.java:59`：`LocalDateTime.of(2100, 12, 06, 12, 34, 34, 973)` —— `setTimeLocal/getTimeLocal` 边界。
- `test/jdk/java/util/zip/TestExtraTime.java:79-81`：`FileTime.from(time, TimeUnit.MILLISECONDS)` 的 mtime/atime/ctime 测试。

## 4. 测试用例设计 + 自动化实现

- **正常**：`getName`、`isDirectory`（`dir/` 为 true）；`setSize/getSize`、`setCompressedSize`、`setCrc`、`setMethod` 往返；`setExtra/getExtra`、`setComment/getComment`。
- **时间**：`setTime(1000L)` 精确回读；`setTimeLocal(LocalDateTime.of(...))` 精确往返；`FileTime` 访问器精确 millis 且返回 `this`。
- **组合**：`clone()` 返回不同实例且复制 name/size/crc；拷贝构造复制 name/size/crc/method；`hashCode` 同 name 相等；`toString` 非空。
- **自动化**：`ZipEntryTest.testBlackBox()`；`testWhiteBox()` 校验 `implements Cloneable`、常量、构造器与方法存在。
- **健壮性**：固定 epoch millis、固定 LocalDateTime、无墙钟、headless、无 `@key`。
