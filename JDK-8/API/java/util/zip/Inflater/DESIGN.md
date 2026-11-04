# java.util.zip.Inflater 测试设计案

> 覆盖目标：**完整契约**。固定输入确定性，headless 安全。

## 1. API 功能

解压器：构造（默认/nowrap）、`setInput`、`inflate`、`needsInput/needsDictionary/finished`、`getRemaining/getAdler/getBytesRead/getBytesWritten`、`reset()`、`end()`。

## 2. Java 用户如何使用

- 与 `Deflater` 配对：压缩端输出喂给 `setInput`，`inflate` 还原。
- 依赖：初始 `needsInput()==true`、`needsDictionary()==false`；nowrap 用裸 DEFLATE。
- 损坏数据抛 `DataFormatException`；`end()` 后使用抛 NPE。

## 3. 参考来源

- OpenJDK 回归 `~/git/jdk8u-dev/jdk/test/java/util/zip`；Inflater 实现源码。

## 4. 测试用例设计

| 测试点 | 用例方法 |
|---|---|
| 往返 / 状态标志 | testRoundTrip |
| nowrap 裸解压 | testNowrap |
| 损坏数据 | testCorruptData |
| reset 复用 | testReset |
| null/越界/use-after-end | testNullAndBounds |
