# java.util.stream.Node$OfDouble（包内接口） 测试设计案

> 覆盖目标：**完整契约**（结构 + 默认方法语义）。headless 安全。

## 1. API 功能

Node 原语特化面：asPrimitiveArray/spliterator/asArray；直接父接口 Node$OfPrimitive（其再 extends Node）。包私有，仅反射锁定。

## 2. Java 用户如何使用

包私有接口不可直接实现：用例经 Class.forName 反射验证方法集、直接父类型与返回类型（asPrimitiveArray 泛型擦除为 Object）。

## 3. 参考来源

java.util.stream 包内实现（Node/Sink 是 pipeline 的中间数据结构）。

## 4. 测试用例设计

| 测试点 | 用例方法 |
|---|---|
| 契约面/父类型/返回类型 | testContracts |
