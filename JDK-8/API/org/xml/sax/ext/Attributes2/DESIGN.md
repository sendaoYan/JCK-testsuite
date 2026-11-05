# Attributes2 — 设计案

## 1. API 功能
Attributes 扩展接口：isSpecified(index)/isDeclared(index)。

## 2. Java 用户如何使用
区分手写属性与 DTD 默认补齐、声明与未声明。

## 3. 开源代码实际用法调研
SAX 2.0.2 extensions javadoc。

## 4. 测试用例设计 + 自动化实现
黑盒：JAXP reader 的活属性表 instanceof Attributes2；显式属性 spec=T/decl=T；未声明属性 spec=T/decl=F；DTD 默认补齐属性值=dv 且 spec=F/decl=T；继承自 org.xml.sax.Attributes。
