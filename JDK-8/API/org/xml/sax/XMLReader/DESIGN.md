# XMLReader — 设计案

## 1. API 功能
SAX2 解析器核心接口：特征/属性访问（set/getFeature/Property）、四 handler 槽（content/dtd/entity/error）、parse(InputSource)/parse(systemId)。

## 2. Java 用户如何使用
JAXP SAXParser.getXMLReader() 取实例；或 helpers.XMLReaderFactory.createXMLReader() 直连驱动。

## 3. 开源代码实际用法调研
JDK test/jdk/org/xml/sax/ptests（特征矩阵与回调序）；SAX 2.0.2 javadoc。

## 4. 测试用例设计 + 自动化实现
黑盒：JAXP 通道默认特征五项（namespaces=F/namespace-prefixes=T/validation=F/string-interning=T/external-general-entities=T）；namespaces×namespace-prefixes 四态矩阵逐态断言（ns=F 时 xmlns 声明以普通属性出现 len=3；ns=T pfx=F 时映射 2 条且声明隐藏 len=1；ns=T pfx=T 时两者并存 len=3、(uri,local) 命中）；handler 槽往返与可空；未知 feature/property → SAXNotRecognizedException；parse(null InputSource)→NPE、parse(null String)→MalformedURLException；同一 reader 二次解析完整重放（每次新 InputSource）。易错：InputSource 包 StringReader 不能复用（Stream closed）。
