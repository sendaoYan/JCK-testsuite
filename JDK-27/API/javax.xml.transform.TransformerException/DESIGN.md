# javax.xml.transform.TransformerException — 设计案

## 1. API 功能
javax.xml.transform.TransformerException 类。实例/声明方法：getLocator, setLocator, getException, getCause, initCause, getMessageAndLocation, getLocationAsString, printStackTrace。

## 2. Java 用户如何使用
new(...) → getException/getCause/getLocator/getLocationAsString。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.transform.TransformerException）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 TransformerExceptionTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`TransformerExceptionTest.testBlackBox()`）覆盖：三种构造 + locator/cause。
