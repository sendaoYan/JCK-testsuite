# java.util.zip.DeflaterInputStream 测试设计案

> 覆盖目标：**完整契约**。headless 安全，临时文件 finally 清理。

## 1. API 功能

FilterInputStream：读取时透明 DEFLATE 压缩；close 幂等；mark 不支持。

## 2. Java 用户如何使用

包一层源流读出压缩字节；available() 反映缓冲；read() 单字节。

## 3. 参考来源

RFC 1951；FilterInputStream 契约。

## 4. 测试用例设计

DeflaterInputStreamTest.testContracts：round-trip（配 InflaterInputStream 还原）、close 后 read IOException、markSupported false。
