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
- 特性矩阵：8 个 FEATURE 常量 getFeature 全 true，常量值精确。
- 属性语义：translet-name round-trip；未知属性 → IAE 含属性名。
- URIResolver 采纳闭环（xsl:include，href/base 精确，输出 `<inc><got id="3">third</got></inc>`）。
- getAssociatedStylesheet：有 PI → systemId endsWith("style.xsl")；无 PI → null。
- 发现失败：系统属性 no.such 工厂 → TCE 含 "no.such"（finally 清理）。
- 编译失败：未闭合标签 → TransformerException 含 "unclosed"；newTransformer(null)/newTemplates(null) → NPE。
- JDK-27 版本面：newDefaultInstance() 存在且产出可用工厂（反射+行为双锁定，JDK-8 无此方法）。
