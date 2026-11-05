# SAXParserFactory — 设计案

## 1. API 功能
SAX 解析器工厂抽象类，产出 SAXParser。newInstance()/newInstance(className,loader)；setNamespaceAware/setValidating/setXIncludeAware/setSchema 与 is* 查询；抽象 setFeature/getFeature。

## 2. Java 用户如何使用
SAXParserFactory.newInstance().newSAXParser() 后 parse(InputSource, DefaultHandler) 接收回调。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 用 SAXParserFactory.newInstance().newSAXParser() 后 getXMLReader 设 ContentHandler/ErrorHandler 解析。

## 4. 测试用例设计 + 自动化实现
黑盒：newInstance 非空；bogus/null→FactoryConfigurationError；默认配置；setter 往返；unknown feature→异常；newSAXParser 非空且 parser 镜像 factory 配置。JDK-27 追加 newDefaultInstance/newNSInstance/newDefaultNSInstance。


## 5. 深化（本批新增：精确异常 + 特性联动）
- setFeature/getFeature(unknown) → SAXNotRecognizedException；null → NPE。
- namespaces/validation 往返；factory 开关 ↔ reader 特性 ↔ 回调 uri/local/qName 三层一致。
- Schema 同一性（factory 与 parser）；validating+DTD 解析完成。
