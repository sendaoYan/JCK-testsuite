# DeclHandler — 设计案

## 1. API 功能
DTD 声明回调：elementDecl/attributeDecl/internalEntityDecl/externalEntityDecl。

## 2. Java 用户如何使用
监听 DTD 结构（schema 探测、文档生成）。

## 3. 开源代码实际用法调研
SAX 2.0.2 extensions javadoc。

## 4. 测试用例设计 + 自动化实现
黑盒：declaration-handler 属性注入+往返+清除；elementDecl 收名称与内容模型原文；attributeDecl 四种形态：默认值 'dx' → mode=null/value=dx、#REQUIRED → mode 固定串、#FIXED → mode+value、普通 #IMPLIED；internal/external 实体声明分别回调。易错：有默认值的声明 mode 为 null（不是 #IMPLIED）。
