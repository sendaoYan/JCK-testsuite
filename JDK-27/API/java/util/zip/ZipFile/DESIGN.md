# java.util.zip.ZipFile 测试设计案

> 覆盖目标：**完整契约**。headless 安全。

## 1. API 功能

读 ZIP 文件：构造（String/File/+mode/+Charset）、`getEntry/getInputStream/entries/stream/size/getComment/getName`、`close()`；实现 `Closeable`；常量 `OPEN_READ/OPEN_DELETE`。

## 2. Java 用户如何使用

- 打开文件，`getEntry(name)` → `getInputStream(entry)` 读内容；`entries()/stream()` 遍历。
- 依赖：name/File null→NPE；缺失文件→FileNotFoundException；`getEntry(null)`/`getInputStream(null)`→NPE；关闭后 size/getComment/getEntry/getInputStream/entries→IllegalStateException（`getName` 仍可用）；close 幂等；`OPEN_DELETE` 需与 `OPEN_READ` 组合。

## 3. 参考来源

- OpenJDK 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/util/zip/ZipFile.java`（`ensureOpen`、mode 校验）。

## 4. 测试用例设计

| 测试点 | 用例方法 |
|---|---|
| 构造 null/缺失文件 | testCtor |
| 读取 / 遍历 | testRead |
| 关闭语义 | testClose |
| OPEN_DELETE | testOpenDelete |
