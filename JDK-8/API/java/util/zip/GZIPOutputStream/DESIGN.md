# java.util.zip.GZIPOutputStream 测试设计案

> 覆盖目标：**完整契约**。headless 安全。

## 1. API 功能

写 gzip：构造（OutputStream / +size / +syncFlush）、`write`、`finish`；继承 `DeflaterOutputStream`。

## 2. Java 用户如何使用

- 写入数据后 `finish()`/`close()`，得到魔数 `1f 8b` + 小端 CRC32/ISIZE trailer。
- 依赖：`size<=0`→IAE；`finish()` 不关闭底层流；close 幂等。

## 3. 参考来源

- OpenJDK 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/util/zip/GZIPOutputStream.java`（gzip trailer 小端序）。

## 4. 测试用例设计

| 测试点 | 用例方法 |
|---|---|
| 往返 + 魔数 | testRoundTrip |
| trailer CRC32/ISIZE | testTrailer |
| 单字节 write | testSingleByteWrite |
| finish 不关闭 | testFinishWithoutClose |
| 构造校验 | testCtorValidation |
| null/越界 | testNullAndBounds |
| close 幂等 | testClose |
