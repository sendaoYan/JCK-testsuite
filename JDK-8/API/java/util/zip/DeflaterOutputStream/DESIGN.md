# java.util.zip.DeflaterOutputStream 测试设计案

> 覆盖目标：**完整契约**。headless 安全，临时文件 finally 清理。

## 1. API 功能

写时压缩；finish() 收尾可继续 flush/close；close 蕴含 finish；同一底层 Deflater 复用需 finish 分界。

## 2. Java 用户如何使用

write(byte[],off,len) 边界；write beyond end of stream 锁定。

## 3. 参考来源

RFC 1951；GZIPOutputStream 依赖其语义。

## 4. 测试用例设计

DeflaterOutputStreamTest.testContracts：压缩 round-trip、finish 后 write ZipException（"write beyond end of stream"）、close 幂等、syncFlush 语义。
