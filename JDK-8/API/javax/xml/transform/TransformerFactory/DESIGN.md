# TransformerFactory — 设计案

## 1. API 功能
XSLT TransformerFactory。newInstance()/newInstance(className,loader)；newTransformer()（恒等）与 newTransformer(Source)/newTemplates(Source)（编译样式表）；setErrorListener/setURIResolver；setAttribute/setFeature。

## 2. Java 用户如何使用
newTransformer() 后 transform(StreamSource, StreamResult) 做恒等往返；newTemplates(最小 XSL 字符串) 编译样式表。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 用 TransformerFactory.newInstance().newTransformer() 做恒等转换并断言输出。

## 4. 测试用例设计 + 自动化实现
黑盒：newInstance 非空；bogus/null→TransformerFactoryConfigurationError；setAttribute/getAttribute null→NPE；setErrorListener(null)→IAE；setFeature unknown→TransformerConfigurationException、getFeature unknown→false；listener/resolver 往返；newTransformer/newTemplates 非空；newTemplates(非样式表)→TransformerConfigurationException。JDK-27 追加 newDefaultInstance。
