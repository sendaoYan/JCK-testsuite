# javax.xml.stream.XMLInputFactory — 设计案

## 1. API 功能
javax.xml.stream.XMLInputFactory 抽象类。常量：IS_NAMESPACE_AWARE, IS_VALIDATING, IS_COALESCING, IS_REPLACING_ENTITY_REFERENCES, IS_SUPPORTING_EXTERNAL_ENTITIES, SUPPORT_DTD, REPORTER, RESOLVER, ALLOCATOR（共 9 个）。 静态入口：newDefaultFactory, newInstance, newFactory。 实例/声明方法：createXMLStreamReader, createXMLEventReader, createFilteredReader, getXMLResolver, setXMLResolver, getXMLReporter, setXMLReporter, setProperty, getProperty, isPropertySupported, setEventAllocator, getEventAllocator。

## 2. Java 用户如何使用
newFactory → createXMLStreamReader(固定 XML) 遍历 START_ELEMENT 局部名 → createXMLEventReader。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.XMLInputFactory）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XMLInputFactoryTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XMLInputFactoryTest.testBlackBox()`）覆盖：流式事件序列、事件读取器、property 支持、未知 property 异常。
