# javax.xml.stream.XMLOutputFactory — 设计案

## 1. API 功能
javax.xml.stream.XMLOutputFactory 抽象类。常量：IS_REPAIRING_NAMESPACES（共 1 个）。 静态入口：newDefaultFactory, newInstance, newFactory。 实例/声明方法：createXMLStreamWriter, createXMLEventWriter, setProperty, getProperty, isPropertySupported。

## 2. Java 用户如何使用
newFactory → createXMLStreamWriter 写固定文档 → 字节级精确输出；createXMLEventWriter 组合事件。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.XMLOutputFactory）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XMLOutputFactoryTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XMLOutputFactoryTest.testBlackBox()`）覆盖：流式写往返、事件写、property 支持、未知 property。
