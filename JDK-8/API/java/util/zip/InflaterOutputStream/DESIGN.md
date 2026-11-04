# java.util.zip.InflaterOutputStream 测试设计案

> 覆盖目标：**完整契约**。headless 安全，临时文件 finally 清理。

## 1. API 功能

写时解压到目标流；finish 后写入（JDK-8 为 NPE "Inflater has been closed"，版本差异）。

## 2. Java 用户如何使用

写压缩数据自动解压；flush 透传。

## 3. 参考来源

javadoc；双 JDK 实测。

## 4. 测试用例设计

InflaterOutputStreamTest.testContracts：round-trip、finish 后 write（双版本 catch 锁定 NPE/IOException）、close 幂等、残余输入边界。
