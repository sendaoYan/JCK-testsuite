# javax.xml.stream.XMLInputFactory — 设计案

## 1. API 功能
javax.xml.stream.XMLInputFactory 抽象类。常量：IS_NAMESPACE_AWARE, IS_VALIDATING, IS_COALESCING, IS_REPLACING_ENTITY_REFERENCES, IS_SUPPORTING_EXTERNAL_ENTITIES, SUPPORT_DTD, REPORTER, RESOLVER, ALLOCATOR（共 9 个）。 静态入口：newDefaultFactory, newInstance, newFactory。 实例/声明方法：createXMLStreamReader, createXMLEventReader, createFilteredReader, getXMLResolver, setXMLResolver, getXMLReporter, setXMLReporter, setProperty, getProperty, isPropertySupported, setEventAllocator, getEventAllocator。

## 2. Java 用户如何使用
newFactory → createXMLStreamReader(固定 XML) 遍历 START_ELEMENT 局部名 → createXMLEventReader。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.XMLInputFactory）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XMLInputFactoryTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XMLInputFactoryTest.testBlackBox()`）覆盖：流式事件序列、事件读取器、property 支持、未知 property 异常。
## 5. 契约深化（本轮追加：属性/通道/行为开关/过滤器）
- 5 布尔属性往返 + 未知名 set/get 双抛 IAE；IS_VALIDATING=true → IllegalArgumentException（JDK 拒绝而非"无效果"，与 JDK-8 一致）；9 属性名常量逐字断言。
- 四通道 createXMLStreamReader（Reader/InputStream/(IS,enc)/Source）的 getEncoding/getCharacterEncodingScheme 语义矩阵。
- IS_COALESCING=true 合并；IS_NAMESPACE_AWARE=false 不拆名；IS_REPLACING_ENTITY_REFERENCES=false → ENTITY_REFERENCE 事件；SUPPORT_DTD=false+实体 → XMLStreamException。
- createFilteredReader 双过滤器（Stream/Event）行为矩阵；reject-all 零事件。
- newDefaultFactory() 行为断言（JDK-9+，JDK-8 无）；newFactory/newInstance 独立实例；系统属性覆盖 + finally 恢复。
