# XMLReaderFactory — 设计案

## 1. API 功能
XMLReader 工厂：createXMLReader()/createXMLReader(className)，读 org.xml.sax.driver 系统属性。

## 2. Java 用户如何使用
SAX2 直连驱动的入口（绕过 JAXP）。

## 3. 开源代码实际用法调研
SAX 2.0.2 helpers javadoc。

## 4. 测试用例设计 + 自动化实现
黑盒：无属性即返回内置 com.sun.org.apache.xerces.internal.parsers.SAXParser（两侧同）；createXMLReader(null)→NPE（javadoc 声称 SAXException，实测 NPE）；org.xml.sax.driver 属性路径生效；产物 reader 可直接挂 handler 解析。位于 org.xml.sax.helpers 包。
