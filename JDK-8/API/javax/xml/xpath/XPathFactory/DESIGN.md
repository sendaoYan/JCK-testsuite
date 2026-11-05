# XPathFactory — 设计案

## 1. API 功能
XPathFactory。newInstance()/newInstance(uri)/newInstance(uri,loader)；isObjectModelSupported(uri)；newXPath()；setFeature/getFeature。JDK-27 追加 newDefaultInstance 与 setProperty/getProperty。

## 2. Java 用户如何使用
XPathFactory.newInstance().newXPath() 后 compile/evaluate 求值。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 用 XPathFactory.newInstance().newXPath() 求值节点集/字符串。

## 4. 测试用例设计 + 自动化实现
黑盒：newInstance 非空；DEFAULT_OBJECT_MODEL_URI 与 DOM_OBJECT_MODEL 相等；newInstance(null uri)→NPE、bogus uri→XPathFactoryConfigurationException；isObjectModelSupported(DOM)=true/bogus=false；newXPath 非空且每次新建。JDK-27 追加 newDefaultInstance 与 setProperty/getProperty unknown→IAE。

## 5. 深化条目（本批新增，XPathFactoryTest 10→24 断言）
- **常量精确**：DEFAULT_PROPERTY_NAME=="javax.xml.xpath.XPathFactory"、DEFAULT_OBJECT_MODEL_URI=="http://java.sun.com/jaxp/xpath/dom"（与 XPathConstants.DOM_OBJECT_MODEL 同值）。
- **Feature 矩阵**：FEATURE_SECURE_PROCESSING true/false 往返；未知 set/get → XPathFactoryConfigurationException；getFeature(null) → NPE。
- **resolver 继承**：工厂级 setXPathVariableResolver/setXPathFunctionResolver → newXPath() 实例 getXPathVariableResolver()==同一实例；带 resolver 工厂产出 $x+1==43.0 闭环。
- **newInstance(uri, className, loader)**：com.sun.org.apache.xpath.internal.jaxp.XPathFactoryImpl 显式加载成功。
- **版本面**：JDK-8 无 newDefaultInstance/setProperty/getProperty（反射断言）。
