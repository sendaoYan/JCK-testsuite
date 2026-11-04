# XPathFactory — 设计案

## 1. API 功能
XPathFactory。newInstance()/newInstance(uri)/newInstance(uri,loader)；isObjectModelSupported(uri)；newXPath()；setFeature/getFeature。JDK-27 追加 newDefaultInstance 与 setProperty/getProperty。

## 2. Java 用户如何使用
XPathFactory.newInstance().newXPath() 后 compile/evaluate 求值。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 用 XPathFactory.newInstance().newXPath() 求值节点集/字符串。

## 4. 测试用例设计 + 自动化实现
黑盒：newInstance 非空；DEFAULT_OBJECT_MODEL_URI 与 DOM_OBJECT_MODEL 相等；newInstance(null uri)→NPE、bogus uri→XPathFactoryConfigurationException；isObjectModelSupported(DOM)=true/bogus=false；newXPath 非空且每次新建。JDK-27 追加 newDefaultInstance 与 setProperty/getProperty unknown→IAE。
