# java.util.zip.Deflater 测试设计案

> 覆盖目标：**完整契约**。固定输入确定性，headless 安全。

## 1. API 功能

压缩器：构造（级别/nowrap）、`setInput`、`deflate`、`setLevel/setStrategy`、`finish/finished/needsInput`、`getBytesRead/getBytesWritten/getAdler`、`reset()`、`end()`。

## 2. Java 用户如何使用

- `setInput` → `finish()` → 循环 `deflate` 直到 `finished()`。
- 依赖：`setLevel` 只接受 [-1,9]（`DEFAULT_COMPRESSION=-1` 例外）；nowrap=裸 DEFLATE；`end()` 后使用抛 NPE。

## 3. 参考来源

- OpenJDK 回归 `~/git/jdk8u-dev/jdk/test/java/util/zip`；Deflater 实现源码。

## 4. 测试用例设计

| 测试点 | 用例方法 |
|---|---|
| 往返 / 状态 | testRoundTrip |
| 级别边界 | testLevels |
| nowrap | testNowrap |
| deflate 重载 | testDeflateOverloads |
| reset 复用 | testReset |
| null/越界/use-after-end | testNullAndBounds |
