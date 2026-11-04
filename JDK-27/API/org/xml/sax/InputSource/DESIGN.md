# org.xml.sax.InputSource — 设计案

## 1. API 功能
org.xml.sax.InputSource 类。实例/声明方法：setPublicId, getPublicId, setSystemId, getSystemId, setByteStream, getByteStream, setEncoding, getEncoding, setCharacterStream, getCharacterStream, isEmpty。

## 2. Java 用户如何使用
四种构造 + setters/getters + isEmpty。

## 3. 开源代码实际用法调研
JDK 源码自用（org.xml.sax.InputSource）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 InputSourceTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`InputSourceTest.testBlackBox()`）覆盖：构造、setter 往返、isEmpty。
