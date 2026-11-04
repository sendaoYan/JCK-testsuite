# java.util.zip.GZIPInputStream 测试设计案

> 覆盖目标：**完整契约**。headless 安全。

## 1. API 功能

读 gzip：构造（InputStream / +size）、`read`、`close`；`GZIP_MAGIC=0x8b1f`；继承 `InflaterInputStream`。

## 2. Java 用户如何使用

- 用 gzip 数据构造，`read` 得解压后字节。
- 依赖：非 gzip 魔数→ZipException；`size<=0`→IAE；支持多成员拼接；读尽返回 -1；关闭后 read→IOException。

## 3. 参考来源

- OpenJDK 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/util/zip/GZIPInputStream.java`。

## 4. 测试用例设计

| 测试点 | 用例方法 |
|---|---|
| 往返 | testRoundTrip |
| 单字节 read | testSingleByteRead |
| 魔数校验 | testMagicValidation |
| 多成员拼接 | testMultiMember |
| 构造校验 | testCtorValidation |
| null/越界 | testNullAndBounds |
| 关闭 | testClose |
