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
- setFeature/getFeature(unknown) → SAXNotRecognizedException（精确非任意）；getFeature(null)/setFeature(null,..) → NPE。
- namespaces/validation 特性往返；factory namespaceAware 开关与 reader 特性一致、可在回调观察到 uri/local/qName 分解。
- Schema set/get/parser 携带同一性；setSchema(null) 清空。
- validating factory + DTD 文档解析完成。
