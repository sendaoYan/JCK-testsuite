# java.util.zip.InflaterInputStream 测试设计案

> 覆盖目标：**完整契约**。headless 安全，临时文件 finally 清理。

## 1. API 功能

读时解压；read()==-1 后 available()==0；skip(n) 精确；close 幂等。

## 2. Java 用户如何使用

配 DeflaterOutputStream 造闭环；skip 后续字节定位。

## 3. 参考来源

RFC 1951；FilterInputStream 契约。

## 4. 测试用例设计

InflaterInputStreamTest.testContracts：round-trip、驱动至 -1 后 available 0、skip(2) 后取 raw[2]、close 后 read IOException。
