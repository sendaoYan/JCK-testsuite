# java.util.stream.Sink（包内接口） 测试设计案

> 覆盖目标：**完整契约**。headless 安全，临时文件 finally 清理。

## 1. API 功能

begin(long)/accept/end 生命周期 + cancellationRequested 协作取消；默认方法集合锁定。

## 2. Java 用户如何使用

包私有——反射锁定默认方法（begin/end/cancellationRequested/accept）；Chained* 抽象类单构造器包裹下游。

## 3. 参考来源

java.util.stream 包内实现。

## 4. 测试用例设计

Sink/OfInt/OfLong/OfDouble/ChainedReference/ChainedInt/ChainedLong/ChainedDouble 各 testContracts 反射锁定。
