# java.util.zip.CheckedInputStream 测试设计案

> 覆盖目标：**完整契约**。headless 安全。

## 1. API 功能

带校验的输入流：构造（InputStream, Checksum）、`read()`、`read(byte[],int,int)`、`skip(long)`、`getChecksum()`；继承 `FilterInputStream`。

## 2. Java 用户如何使用

- 包裹输入流读取数据，`getChecksum().getValue()` 得到累加校验值。
- 依赖：`getChecksum()` 返回传入的同一 Checksum 实例；read/skip 都会更新校验和；len-0 read 不更新。

## 3. 参考来源

- OpenJDK 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/util/zip/CheckedInputStream.java`。

## 4. 测试用例设计

| 测试点 | 用例方法 |
|---|---|
| 实例同一性 + read | testIdentityAndRead |
| skip 更新校验和 | testSkip |
| null/越界 | testNullAndBounds |
| len-0 | testLenZero |
