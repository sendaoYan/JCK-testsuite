# Parser — 设计案

## 1. API 功能
SAX1 解析器接口：setLocale/setEntityResolver/setDTDHandler/setDocumentHandler/setErrorHandler + parse(InputSource)/parse(systemId)。

## 2. Java 用户如何使用
JAXP SAXParser.getParser() 返回其现代实现（JAXPSAXParser）。

## 3. 开源代码实际用法调研
SAX 1.0 javadoc；JDK JAXP 桥接。

## 4. 测试用例设计 + 自动化实现
黑盒：JAXP 通道拿实现类直接调 parse+setDocumentHandler（HandlerBase）跑通 SAX1 事件流；parse(null InputSource)→NPE；setLocale 被容忍；接口方法集锁定。
