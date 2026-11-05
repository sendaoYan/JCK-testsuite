# javax.xml.stream.util.XMLEventConsumer — 设计案

## 1. API 功能
javax.xml.stream.util.XMLEventConsumer 接口。实例/声明方法：add。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.stream.util.XMLEventConsumer() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.util.XMLEventConsumer）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XMLEventConsumerTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XMLEventConsumerTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 5. 契约深化条目（2026-11，testDeep()）
- XMLEventWriter instanceof XMLEventConsumer。
- 收集闭环：`<r a='1'>t<c/></r>` 整流 7 事件（序 7,1,4,1,2,2,8）+ SE 属性/CH 文本/ED 逐项复核。
- 写出闭环：reader→writer 输出全等 `<?xml version="1.0" ?><r a="1">t<c></c></r>`；add(XMLEventReader) 整流拷贝同串。
- null/异常：自定义 consumer 收 null 无约束；XMLEventWriter.add(null)→NPE；consumer 抛出的异常对象原样中继（==）。
