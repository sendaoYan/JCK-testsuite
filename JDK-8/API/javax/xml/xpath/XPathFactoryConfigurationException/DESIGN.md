# javax.xml.xpath.XPathFactoryConfigurationException — 设计案

## 1. API 功能
javax.xml.xpath.XPathFactoryConfigurationException extends XPathException：工厂配置失败。构造器 (String)/(Throwable)。

## 2. Java 用户如何使用
XPathFactory.newInstance(不支持的 objectModel) 抛出（checked）。

## 3. 参考来源
Oracle javadoc；OpenJDK XPathFactoryImpl 实测。

## 4. 测试用例设计 + 易错细节（XPathFactoryConfigurationExceptionTest）
- **构造链**：(String)/(Throwable) 往返 + 消息派生；恰好 2 个公共构造器；is-a XPathException。
- **真实触发**：newInstance("http://bogus.object.model") → 消息含对象模型 URI（"No XPathFactory implementation found for the object model: ..."）。
