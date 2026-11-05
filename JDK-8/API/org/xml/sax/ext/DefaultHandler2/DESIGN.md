# DefaultHandler2 — 设计案

## 1. API 功能
SAX2 全能基类：继承 DefaultHandler 并实现 LexicalHandler/DeclHandler/EntityResolver2。

## 2. Java 用户如何使用
一个对象同时挂 content/lexical/declaration 三槽。

## 3. 开源代码实际用法调研
SAX 2.0.2 extensions javadoc。

## 4. 测试用例设计 + 自动化实现
黑盒：类型关系四接口；同一实例挂三槽解析带 DTD+注释+CDATA 文档；子类覆写 comment/startDTD/attributeDecl/startCDATA 一次性收齐三通道事件且保序（startDTD→attDecl→CDATA）；getExternalSubset 默认 null。
