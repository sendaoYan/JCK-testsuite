# java.util.zip.ZipInputStream 测试设计案

> 覆盖目标：**完整契约**。headless 安全。

## 1. API 功能

读 ZIP：构造（InputStream / +Charset）、`getNextEntry()`、`closeEntry()`、`read`、`skip`、`available()`、`close()`；继承 `InflaterInputStream`。

## 2. Java 用户如何使用

- `while ((e = getNextEntry()) != null) { 读当前条目; closeEntry(); }`。
- 依赖：读尽返回 null；`available()` 条目结束前 1、结束后 0；`skip(-1)`→IAE；关闭后 read/getNextEntry→IOException；DEFLATED 条目用数据描述符时 size/crc 读完后才可知。

## 3. 参考来源

- OpenJDK 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/util/zip/ZipInputStream.java`。

## 4. 测试用例设计

| 测试点 | 用例方法 |
|---|---|
| 多条目往返 STORED/DEFLATED | testRoundTrip |
| 元数据（size/crc/time） | testMetadata |
| skip / closeEntry | testSkipAndCloseEntry |
| null/越界 | testNullAndBounds |
| 关闭后行为 | testClose |
