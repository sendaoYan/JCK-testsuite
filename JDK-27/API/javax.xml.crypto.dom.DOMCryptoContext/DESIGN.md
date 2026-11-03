# javax.xml.crypto.dom.DOMCryptoContext — 设计案

## 1. API 功能
javax.xml.crypto.dom.DOMCryptoContext 类。实例/声明方法：getNamespacePrefix, putNamespacePrefix, getDefaultNamespacePrefix, setDefaultNamespacePrefix, getBaseURI, setBaseURI, getURIDereferencer, setURIDereferencer, getProperty, setProperty, getKeySelector, setKeySelector, getElementById, setIdAttributeNS, iterator, get, put。

## 2. Java 用户如何使用
经 DOMSignContext 实例化 → baseURI/命名空间前缀/属性/Map/id 属性映射的组合读写。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.crypto.dom.DOMCryptoContext）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 DOMCryptoContextTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`DOMCryptoContextTest.testBlackBox()`）覆盖：各字段默认 null、putNamespacePrefix 首次返回 null、getNamespacePrefix fallback、setIdAttributeNS+getElementById、iterator 计数。。
