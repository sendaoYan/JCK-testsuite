# Transformer — 设计案

## 1. API 功能
XSLT 转换器。transform(Source, Result) 执行转换；setParameter/getParameter/clearParameters；setOutputProperty/getOutputProperty/setOutputProperties/getOutputProperties；setErrorListener/setURIResolver；reset()。

## 2. Java 用户如何使用
newTransformer() 后 transform(StreamSource(内嵌 XML), StreamResult(StringWriter)) 断言往返输出。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 用 Transformer 做恒等转换与参数替换断言。

## 4. 测试用例设计 + 自动化实现
黑盒：恒等转换往返断言 xml 声明/根元素/属性/文本；transform(null source)→TransformerException；参数 set/get/clear 与 null 值→IAE；输出属性默认 method=xml、set/setOutputProperties 往返、null name→NPE；setErrorListener(null)→IAE；listener/resolver 往返；reset 后可复用。
