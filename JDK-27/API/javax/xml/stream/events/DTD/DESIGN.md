# javax.xml.stream.events.DTD — 设计案

## 1. API 功能
javax.xml.stream.events.DTD 接口。实例/声明方法：getDocumentTypeDeclaration, getProcessedDTD, getNotations, getEntities。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.stream.events.DTD() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.events.DTD）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 DTDTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`DTDTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 5. 契约深化条目（2026-11，testDeep()）
- 完整内部子集：声明串与原文全等（含换行/引号）；entities(e1,e2)/notations(bin,gif) 列表；getProcessedDTD()=null。
- 空内部子集 `<!DOCTYPE r >`：串全等、两列表 null；无 DOCTYPE：无 DTD 事件。
- 工厂 createDTD：串 round-trip；列表全 null；码 11；编码=输入串。
- javap 实测无 processAny 之类方法（仅 getDocumentTypeDeclaration/getProcessedDTD/getEntities/getNotations）。
