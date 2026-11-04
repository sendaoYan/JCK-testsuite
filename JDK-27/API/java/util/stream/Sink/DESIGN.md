# java.util.stream.Sink（包内接口，JDK-27 侧） 测试设计案

> 覆盖目标：**完整契约**。headless 安全，临时文件 finally 清理。

## 1. API 功能

同 JDK-8 契约面（反射锁定，与 JDK-8 一致）。

## 2. Java 用户如何使用

默认方法集合与 Chained* 结构跨版本不变。

## 3. 参考来源

java.util.stream 包内实现。

## 4. 测试用例设计

与 JDK-8 同名 testContracts。
