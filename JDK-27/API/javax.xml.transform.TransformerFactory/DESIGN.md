# javax.xml.transform.TransformerFactory — 设计案

## 1. API 功能
javax.xml.transform.TransformerFactory 抽象类。静态入口：newDefaultInstance, newInstance。 实例/声明方法：newTransformer, newTemplates, getAssociatedStylesheet, setURIResolver, getURIResolver, setFeature, getFeature, setAttribute, getAttribute, setErrorListener, getErrorListener。

## 2. Java 用户如何使用
newInstance → newTransformer → transform(DOM→StreamResult) 精确输出；newTemplates 用固定 XSLT 转出 <out>。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.transform.TransformerFactory）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 TransformerFactoryTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`TransformerFactoryTest.testBlackBox()`）覆盖：identity transform、error listener/attribute 往返、secure-processing feature、模板编译+执行。
