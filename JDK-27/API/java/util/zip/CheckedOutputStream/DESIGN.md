# java.util.zip.CheckedOutputStream 测试设计案

> 覆盖目标：**完整契约**。headless 安全。

## 1. API 功能

带校验的输出流：构造（OutputStream, Checksum）、`write(int)`、`write(byte[],int,int)`、`getChecksum()`；继承 `FilterOutputStream`。

## 2. Java 用户如何使用

- 包裹输出流写数据，`getChecksum().getValue()` 得到累加校验值；底层流仍收到全部字节。
- 依赖：`getChecksum()` 返回传入的同一实例；两种 write 都更新校验和。

## 3. 参考来源

- OpenJDK 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/util/zip/CheckedOutputStream.java`。

## 4. 测试用例设计

| 测试点 | 用例方法 |
|---|---|
| 实例同一性 + write | testIdentityAndWrite |
| null/越界 | testNullAndBounds |
