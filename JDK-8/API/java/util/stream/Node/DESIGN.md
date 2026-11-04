# java.util.stream.Node（包内接口） 测试设计案

> 覆盖目标：**完整契约**。headless 安全，临时文件 finally 清理。

## 1. API 功能

流的中间结果树节点：asArray/copyInto/count/forEach/getChild/getShape/spliterator/truncate；Builder 子接口构建并可 build()。

## 2. Java 用户如何使用

包私有——仅反射锁定契约面（方法集、OfPrimitive 泛型链、Builder extends Sink）。

## 3. 参考来源

java.util.stream 包内实现。

## 4. 测试用例设计

Node/OfPrimitive/OfInt/OfLong/OfDouble/Builder×4 各 testContracts 反射锁定。
