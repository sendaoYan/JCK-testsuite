# java.util.zip.Adler32 测试设计案

> 覆盖目标：**完整契约**。固定向量确定性，headless 安全。

## 1. API 功能

`Checksum` 实现：`update(int)`、`update(byte[],int,int)`、`update(ByteBuffer)`、`getValue()`、`reset()`；初值 1。

## 2. Java 用户如何使用

- 逐步累加后 `getValue()` 取 32 位 Adler-32。
- 依赖初值 1、空数据=1、`reset()` 回到 1。

## 3. 参考来源

- 已知向量：`"Wikipedia"` → `0x11E60398`，`"123456789"` → `0x091E01DE`。
- OpenJDK 回归 `~/git/jdk8u-dev/jdk/test/java/util/zip`。

## 4. 测试用例设计

| 测试点 | 用例方法 |
|---|---|
| 已知向量 / 初值 | testKnownVectors |
| update 重载 | testUpdateOverloads |
| 分段累加等价 | testPartialAndAccumulation |
| reset | testReset |
| null/越界 | testNullAndBounds |
