# java.util.zip.CRC32 测试设计案

> 覆盖目标：**完整契约**。固定向量确定性，headless 安全。

## 1. API 功能

`Checksum` 实现：`update(int)`、`update(byte[],int,int)`、`update(ByteBuffer)`、`getValue()`、`reset()`；初值 0。

## 2. Java 用户如何使用

- 单字节/字节数组/ByteBuffer 逐步累加，最后 `getValue()` 取 32 位 CRC。
- 依赖初值 0、空数据 CRC=0、`reset()` 回到 0 且后续相同输入复现相同值。

## 3. 参考来源

- 已知向量：`"123456789"` → `0xCBF43926`，pangram → `0x414FA339`（RFC/标准测试向量）。
- OpenJDK 回归 `~/git/jdk8u-dev/jdk/test/java/util/zip`。

## 4. 测试用例设计

| 测试点 | 用例方法 |
|---|---|
| 已知向量 / 初值 | testKnownVectors |
| update 重载（byte[]/int/ByteBuffer） | testUpdateOverloads |
| 分段累加等价 | testPartialAndAccumulation |
| reset / 幂等 | testResetAndIdempotence |
| null/越界 | testNullAndBounds |
