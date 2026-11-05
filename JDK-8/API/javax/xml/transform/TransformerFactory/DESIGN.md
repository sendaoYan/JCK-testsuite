# TransformerFactory — 设计案

## 1. API 功能
XSLT TransformerFactory。newInstance()/newInstance(className,loader)；newTransformer()（恒等）与 newTransformer(Source)/newTemplates(Source)（编译样式表）；setErrorListener/setURIResolver；setAttribute/setFeature。

## 2. Java 用户如何使用
newTransformer() 后 transform(StreamSource, StreamResult) 做恒等往返；newTemplates(最小 XSL 字符串) 编译样式表。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 用 TransformerFactory.newInstance().newTransformer() 做恒等转换并断言输出。

## 4. 测试用例设计 + 自动化实现
黑盒：newInstance 非空；bogus/null→TransformerFactoryConfigurationError；setAttribute/getAttribute null→NPE；setErrorListener(null)→IAE；setFeature unknown→TransformerConfigurationException、getFeature unknown→false；listener/resolver 往返；newTransformer/newTemplates 非空；newTemplates(非样式表)→TransformerConfigurationException。JDK-27 追加 newDefaultInstance。

## 5. 深化（本轮新增）
- 特性矩阵：getFeature 对 8 个 FEATURE 常量（含 SAXTransformerFactory.FEATURE/FEATURE_XMLFILTER、DOM/SAX/Stream/StAX Source/Result feature）全部 true；常量字符串值精确锁定。
- 属性语义：setAttribute("translet-name","X") 后 getAttribute 同值 round-trip；未知属性 set/get → IllegalArgumentException 且消息含属性名。
- URIResolver 采纳闭环：xsl:include 场景 resolver 收到 href="inc.xsl"/base="http://base/main.xsl"，返回内嵌 Source 后输出 `<inc><got id="3">third</got></inc>` 精确。
- getAssociatedStylesheet：xml-stylesheet PI 指向 style.xsl → Source.getSystemId() endsWith("style.xsl")；无 PI → null（探针锁定）。
- 发现失败：系统属性指向 no.such 工厂 → TransformerConfigurationException 消息含 "no.such"（finally 清属性）。
- 编译失败：未闭合标签 → TransformerException 含 "unclosed"；newTransformer(null)/newTemplates(null) → NPE（非 TCE，探针锁定）。
- JDK-8 版本面（反射锁定）：无 newDefaultInstance()（JDK-27 才有）。
