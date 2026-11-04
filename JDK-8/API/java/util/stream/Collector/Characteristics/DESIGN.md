# java.util.stream.Collector.Characteristics 测试设计案

> 覆盖目标：**完整契约**。headless 安全，临时文件 finally 清理。

## 1. API 功能

三个枚举值 CONCURRENT/UNORDERED/IDENTITY_FINISH 的组合语义。

## 2. Java 用户如何使用

Collector.of 4 参（恒等 finisher）隐式附加 IDENTITY_FINISH；5 参非恒等则无。

## 3. 参考来源

Collector javadoc。

## 4. 测试用例设计

CollectorTest.testContracts 特性位块。
