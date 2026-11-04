# java.util.zip.DataFormatException 测试设计案

> 覆盖目标：**完整契约**。headless 安全，临时文件 finally 清理。

## 1. API 功能

受检异常，inflate 遇畸形 zlib 数据时抛出。

## 2. Java 用户如何使用

catch (DataFormatException) 显式处理；携带 cause 链。

## 3. 参考来源

RFC 1950；Inflater.inflate javadoc。

## 4. 测试用例设计

DataFormatExceptionTest.testContracts：构造器（无参/消息/cause）、getCause、无输入 inflate 返回 0 不抛。
