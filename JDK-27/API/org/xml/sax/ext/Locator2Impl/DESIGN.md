# org.xml.sax.ext.Locator2Impl — 设计案

## 1. API 功能
org.xml.sax.ext.Locator2Impl 类。实例/声明方法：getXMLVersion, getEncoding, setXMLVersion, setEncoding。

## 2. Java 用户如何使用
setXMLVersion/setEncoding + copy。

## 3. 开源代码实际用法调研
JDK 源码自用（org.xml.sax.ext.Locator2Impl）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 Locator2ImplTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`Locator2ImplTest.testBlackBox()`）覆盖：版本/编码 + 拷贝。
