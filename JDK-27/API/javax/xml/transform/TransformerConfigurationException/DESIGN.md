# javax.xml.transform.TransformerConfigurationException — 设计案

## 1. API 功能
javax.xml.transform.TransformerConfigurationException 类。标记接口，无声明成员。

## 2. Java 用户如何使用
new(String)/new()。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.transform.TransformerConfigurationException）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 TransformerConfigurationExceptionTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`TransformerConfigurationExceptionTest.testBlackBox()`）覆盖：两种构造 + 继承。
