# org.xml.sax.AttributeList — 设计案

## 1. API 功能
org.xml.sax.AttributeList 接口。实例/声明方法：getLength, getName, getType, getValue。

## 2. Java 用户如何使用
匿名实现 `new org.xml.sax.AttributeList() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.xml.sax.AttributeList）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 AttributeListTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`AttributeListTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

### 深化轮次2（行为契约）
黑盒：JAXP getParser() 通道（SAXParserImpl$JAXPSAXParser 直接实现 org.xml.sax.Parser）真解析回调中取活表：len/名字查找/缺名 null/越界 getName(9)=null/type=CDATA；HandlerBase 通道等效；接口形态锁定。
