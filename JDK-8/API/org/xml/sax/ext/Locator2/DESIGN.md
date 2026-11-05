# Locator2 — 设计案

## 1. API 功能
Locator 扩展：getXMLVersion/getEncoding。

## 2. Java 用户如何使用
探测 XML 声明的版本与编码。

## 3. 开源代码实际用法调研
SAX 2.0.2 extensions javadoc。

## 4. 测试用例设计 + 自动化实现
黑盒：JAXP reader 交付的是 Locator2；setDocumentLocator 时刻 version 仍 '1.0'（声明未读）；首元素时刻反映 1.1 声明；字符流源 encoding=null。易错：version 是渐进揭示的。
