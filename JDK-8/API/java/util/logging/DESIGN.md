# java.util.logging 包 测试设计案

> 覆盖目标：**完整契约**。headless 安全，临时文件 finally 清理。

## 1. API 功能

Logger 层级/门控/监听、Handler 家族（Memory/File/Stream/Console/Socket）、Formatter（Simple/XML）、Level 解析矩阵、LogRecord 字段、LogManager 配置生命周期。

## 2. Java 用户如何使用

entering/exiting/throwing 捕获 ENTRY/RETURN/THROW；MemoryHandler pushLevel 触发整体下发；FileHandler %t/%u/轮转；XMLFormatter 元素与转义；Level.parse("800")==INFO。

## 3. 参考来源

JUL javadoc；readConfiguration 重置语义。

## 4. 测试用例设计

17 个测试各 testContracts()（临时目录 + finally 清理 + 配置恢复）。
