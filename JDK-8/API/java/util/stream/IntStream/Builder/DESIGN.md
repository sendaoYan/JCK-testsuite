# java.util.stream.IntStream.Builder 测试设计案

> 覆盖目标：**完整契约**。headless 安全，临时文件 finally 清理。

## 1. API 功能

原语 int 消费器状态机；同 Stream.Builder 语义。

## 2. Java 用户如何使用

toArray 精确内容比对。

## 3. 参考来源

javadoc。

## 4. 测试用例设计

IntStreamBuilderTest.testContracts。
