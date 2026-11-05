# DocumentBuilderFactory — 设计案

## 1. API 功能
DOM 解析器工厂抽象类，产出 DocumentBuilder。核心：newInstance()/newInstance(className,loader) 工厂；setNamespaceAware/setValidating/setXIncludeAware/setSchema 等配置开关与对应 is* 查询；抽象 setAttribute/getAttribute、setFeature/getFeature。

## 2. Java 用户如何使用
DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(内嵌 XML 字符串) → Document 遍历。配置项 setNamespaceAware(true) 后解析命名空间文档。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 大量使用 newInstance→newDocumentBuilder→parse(InputSource) 解析内联 XML 断言 DOM 结构；Spring DomUtils/DocumentLoader 组合解析后 getElementsByTagName 提取 Bean。

## 4. 测试用例设计 + 自动化实现
黑盒：newInstance 非空；显式 impl 类名加载；bogus/null 类名→FactoryConfigurationError；默认配置（namespaceAware/validating/xIncludeAware=false、schema=null）；全量 setter 往返；setAttribute/getAttribute 的 null→IAE/NPE 与 unknown→IAE；setFeature/getFeature unknown→ParserConfigurationException 与 null→NPE；newDocumentBuilder 非空且每次新建。JDK-27 追加 newDefaultInstance/newNSInstance/newDefaultNSInstance。

## 5. 深化（本批新增：特性开关行为矩阵）
- coalescing=true → CDATA+文本合并单 TEXT；false → [TEXT,CDATA,TEXT] 三节点。
- ignoringComments=true → 内层与文档级注释均消失（实测有效）。
- namespaceAware → getLocalName null vs 值 / nodeName qname 形态 / nsURI+prefix。
- expandEntityReferences=false → ENTITY_REFERENCE(5) 节点；root getTextContent JDK-8 仍展开（"expanded"，27 为 ""，双侧各自锁定）。
- setFeature/getFeature（namespaces/validation）往返；accessExternalDTD 属性 "all"/""/null 往返。
- Schema set/get 同一性；builder 镜像 factory 的 namespaceAware/validating。
