# org.xml.sax.SAXNotSupportedException — 设计案

## 1. API 功能
org.xml.sax.SAXNotSupportedException 类。标记接口，无声明成员。

## 2. Java 用户如何使用
两种构造。

## 3. 开源代码实际用法调研
JDK 源码自用（org.xml.sax.SAXNotSupportedException）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 SAXNotSupportedExceptionTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`SAXNotSupportedExceptionTest.testBlackBox()`）覆盖：message/no-arg。

### 深化轮次2（行为契约）
黑盒：真实触发——JAXPSAXParser 的 is-standalone 特征 get/set 均抛本异常（识别但不可变）；lexical-handler 属性赋错型值抛本异常；与 SAXNotRecognizedException 区分断言。
