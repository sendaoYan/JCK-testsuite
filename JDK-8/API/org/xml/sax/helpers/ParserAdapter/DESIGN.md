# ParserAdapter — 设计案

## 1. API 功能
SAX1 Parser → SAX2 XMLReader 适配器：ns/namespace-prefixes 特征可调。

## 2. Java 用户如何使用
给老 SAX1 parser 套现代接口。

## 3. 开源代码实际用法调研
SAX 2.0.2 helpers javadoc。

## 4. 测试用例设计 + 自动化实现
黑盒：ctor(null)→NPE；ns 特征默认 true、np 默认 false；JAXP getParser() 包装后真解析：SAX1 数据被合成为带命名空间的 startElement(urn:d|r|r)；np=true 保留原始 qName；ns=false 时 uri/local 均空串、qName 原样（'||p:r'）；handler 四槽可挂可空。
