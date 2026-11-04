# java.util.zip.Checksum 测试设计案

> 覆盖目标：**完整接口契约**。headless 安全。

## 1. API 功能

接口：`update(int)`、`update(byte[],int,int)`、`getValue()`、`reset()`。

## 2. Java 用户如何使用

- 实现累加逻辑；调用方逐步 update 后 getValue，reset 归零。
- 依赖：`update(byte[],off,len)` 只处理 [off,off+len) 切片；多次 update 累加；零长度 update 不改变值。

## 3. 参考来源

- OpenJDK 接口定义 + CRC32/Adler32 实现语义。

## 4. 测试用例设计

| 测试点 | 用例方法 |
|---|---|
| 基本 update/reset | testChecksum |
| 切片 + 累加 | testSliceAndAccumulation |
