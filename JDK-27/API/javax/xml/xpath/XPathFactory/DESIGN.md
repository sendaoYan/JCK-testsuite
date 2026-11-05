# XPathFactory — 设计案

## 1. API 功能
XPathFactory。newInstance()/newInstance(uri)/newInstance(uri,loader)；isObjectModelSupported(uri)；newXPath()；setFeature/getFeature。JDK-27 追加 newDefaultInstance 与 setProperty/getProperty。

## 2. Java 用户如何使用
XPathFactory.newInstance().newXPath() 后 compile/evaluate 求值。

## 3. 开源代码实际用法调研
JDK test/jdk/javax/xml/jaxp 用 XPathFactory.newInstance().newXPath() 求值节点集/字符串。

## 4. 测试用例设计 + 自动化实现
黑盒：newInstance 非空；DEFAULT_OBJECT_MODEL_URI 与 DOM_OBJECT_MODEL 相等；newInstance(null uri)→NPE、bogus uri→XPathFactoryConfigurationException；isObjectModelSupported(DOM)=true/bogus=false；newXPath 非空且每次新建。JDK-27 追加 newDefaultInstance 与 setProperty/getProperty unknown→IAE。

## 5. 深化条目（本批新增，XPathFactoryTest 13→29 断言）
- **常量精确**：DEFAULT_PROPERTY_NAME/DEFAULT_OBJECT_MODEL_URI 精确值。
- **Feature 矩阵**：FSP 往返、未知 → XPathFactoryConfigurationException、getFeature(null) → NPE。
- **JDK-19+ setProperty/getProperty 往返**：jdk.xml.xpathExprGrpLimit 设 "50" 取 "50"；setProperty(null 名) → NPE。
- **resolver 继承闭环**：工厂级两 resolver 被 newXPath() 继承；$x+1==43.0。
- newInstance(uri, className, loader) 显式内部实现类。
- **版本面**：newDefaultInstance/setProperty/getProperty 存在（反射断言）。
