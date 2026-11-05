# org.xml.sax.ext.Locator2 — 设计案

## 1. API 功能
org.xml.sax.ext.Locator2 接口。实例/声明方法：getXMLVersion, getEncoding。

## 2. Java 用户如何使用
匿名实现 `new org.xml.sax.ext.Locator2() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.xml.sax.ext.Locator2）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 Locator2Test 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`Locator2Test.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

### 深化轮次2（行为契约）
黑盒：setDocumentLocator 时刻 version 仍 '1.0'、首元素时刻反映 1.1 声明（渐进揭示）；字符流源 encoding=null；交付对象 instanceof Locator2。
