# javax.xml.transform.stream.StreamSource — 设计案

## 1. API 功能
javax.xml.transform.stream.StreamSource 类。常量：FEATURE（共 1 个）。 实例/声明方法：setInputStream, getInputStream, setReader, getReader, setPublicId, getPublicId, setSystemId, getSystemId, isEmpty。

## 2. Java 用户如何使用
new StreamSource(stream/reader/systemId)。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.transform.stream.StreamSource）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 StreamSourceTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`StreamSourceTest.testBlackBox()`）覆盖：四种构造。
