# org.xml.sax.ext.DeclHandler — 设计案

## 1. API 功能
org.xml.sax.ext.DeclHandler 接口。实例/声明方法：elementDecl, attributeDecl, internalEntityDecl, externalEntityDecl。

## 2. Java 用户如何使用
匿名实现 `new org.xml.sax.ext.DeclHandler() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.xml.sax.ext.DeclHandler）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 DeclHandlerTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`DeclHandlerTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

### 深化轮次2（行为契约）
黑盒：declaration-handler 属性注入+往返+清除；elementDecl 收内容模型原文；attributeDecl 形态矩阵：默认值→mode=null、#REQUIRED→mode 串、#FIXED→mode+value；internal/external 实体声明分别回调。易错：有默认值的声明 mode 为 null。
