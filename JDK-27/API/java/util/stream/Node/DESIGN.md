# java.util.stream.Node（包内接口，JDK-27 侧） 测试设计案

> 覆盖目标：**完整契约**。headless 安全，临时文件 finally 清理。

## 1. API 功能

同 JDK-8 契约面（反射锁定，与 JDK-8 一致）。

## 2. Java 用户如何使用

方法集不变性即跨版本契约锁定。

## 3. 参考来源

java.util.stream 包内实现。

## 4. 测试用例设计

与 JDK-8 同名 testContracts。
