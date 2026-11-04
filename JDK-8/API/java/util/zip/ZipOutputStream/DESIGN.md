# java.util.zip.ZipOutputStream 测试设计案

> 覆盖目标：**完整契约**。headless 安全。

## 1. API 功能

写 ZIP：构造（OutputStream / +Charset）、`setComment/setMethod/setLevel`、`putNextEntry`、`closeEntry`、`write`、`finish`、`close`；继承 `DeflaterOutputStream`。

## 2. Java 用户如何使用

- `putNextEntry(entry)` → 写数据 → `closeEntry()`，循环；最后 `finish()`/`close()`。
- 依赖：未 putNextEntry 就 write→ZipException；重名条目→ZipException；STORED 需 size/csize/crc 齐备；`setMethod(非法)`/`setLevel(越界)`/`setComment(过长)`→IAE；finish/close 幂等。

## 3. 参考来源

- OpenJDK 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/util/zip/ZipOutputStream.java`。

## 4. 测试用例设计

| 测试点 | 用例方法 |
|---|---|
| set* 校验 | testValidation |
| 状态机（写前/重名/STORED 前置） | testStateMachine |
| STORED 写越界 | testStoredWritePastEnd |
| STORED 短写 closeEntry | testStoredSizeMismatchOnCloseEntry |
| 往返 + comment | testRoundTripAndComment |
| null/越界 | testNullAndBounds |
