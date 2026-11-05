# javax.xml.xpath.XPathFactoryConfigurationException — 设计案

## 1. API 功能
javax.xml.xpath.XPathFactoryConfigurationException 类。标记接口，无声明成员。

## 2. Java 用户如何使用
new(String)。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.xpath.XPathFactoryConfigurationException）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 XPathFactoryConfigurationExceptionTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`XPathFactoryConfigurationExceptionTest.testBlackBox()`）覆盖：构造 + 继承链。

## 5. 深化条目（本批新增，XPathFactoryConfigurationExceptionTest 6→12 断言）
- 构造链 (String)/(Throwable)；恰好 2 构造器；is-a XPathException。
- 真实触发 newInstance(bogus 模型) 消息含模型 URI。
