# LexicalHandler — 设计案

## 1. API 功能
词法事件：startDTD/endDTD/startEntity/endEntity/startCDATA/endCDATA/comment。

## 2. Java 用户如何使用
CDATA 段、注释、DTD 边界、实体展开边界感知。

## 3. 开源代码实际用法调研
SAX 2.0.2 extensions javadoc。

## 4. 测试用例设计 + 自动化实现
黑盒：lexical-handler 属性注入往返；startDTD(name,pid,sid) 三参；文档注释与 DTD 内注释都回调；CDATA 括号事件成对；内部实体展开被 startEntity/endEntity 包裹；事件顺序锁定。
