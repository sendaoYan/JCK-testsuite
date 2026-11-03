# org.xml.sax.ext.DefaultHandler2 — 设计案

## 1. API 功能
org.xml.sax.ext.DefaultHandler2 类。实例/声明方法：startCDATA, endCDATA, startDTD, endDTD, startEntity, endEntity, comment, attributeDecl, elementDecl, externalEntityDecl, internalEntityDecl, getExternalSubset, resolveEntity。

## 2. Java 用户如何使用
new DefaultHandler2 子类覆盖 startCDATA/comment → parse 触发词法事件。

## 3. 开源代码实际用法调研
JDK 源码自用（org.xml.sax.ext.DefaultHandler2）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 DefaultHandler2Test 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`DefaultHandler2Test.testBlackBox()`）覆盖：3 接口、词法事件回调。
