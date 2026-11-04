# java.util.zip.ZipEntry 测试设计案

> 覆盖目标：**完整契约**。headless 安全。

## 1. API 功能

ZIP 条目元数据：`name`（构造）、`get/setSize`、`get/setCompressedSize`、`get/setCrc`、`get/setTime`、`get/setComment`、`get/setExtra`、`get/setMethod`、`isDirectory()`、`clone()`、`equals/hashCode`。

## 2. Java 用户如何使用

- 构造后通过 setter 填元数据，交给 `ZipOutputStream` 写入。
- 依赖：name null→NPE、name 过长→IAE、extra 过长→IAE；`setExtra` 保留引用（不防御拷贝）；`equals/hashCode` 为恒等（未覆写）；`clone` 拷贝字段；尾斜杠判定目录。

## 3. 参考来源

- OpenJDK 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/util/zip/ZipEntry.java`（`setExtra0` 直接赋值、未覆写 equals）。

## 4. 测试用例设计

| 测试点 | 用例方法 |
|---|---|
| 名称校验 | testNameValidation |
| 元数据往返 | testMetadata |
| method/time | testMethods |
| 目录判定 | testDirectory |
| clone | testClone |
| equals/hashCode 恒等 | testEqualsHashCode |
