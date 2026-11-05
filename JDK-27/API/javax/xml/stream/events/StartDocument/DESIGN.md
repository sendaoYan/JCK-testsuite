# javax.xml.stream.events.StartDocument — 设计案

## 1. API 功能
javax.xml.stream.events.StartDocument 接口。实例/声明方法：getSystemId, getCharacterEncodingScheme, encodingSet, isStandalone, standaloneSet, getVersion。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.stream.events.StartDocument() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.events.StartDocument）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 StartDocumentTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`StartDocumentTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 5. 契约深化条目（2026-11，testDeep()）
- 解析 XMLDecl 五形态矩阵（version/standalone yes/no/缺省/encoding 有无/完全无 decl）；
  **standaloneSet 缺省时 JDK-27=false（规范正确）；JDK-8=true（差异，SA_SET_WHEN_ABSENT 参数化两侧各自锁定）**。
- Reader 输入 getCharacterEncodingScheme()=null；InputStream 输入返回 "UTF-8"；StreamSource systemId 透传。
- 工厂四形态矩阵（无参默认 UTF-8/1.0/sa=T/saSet=F/encSet=T/systemId=""）+ 编码串 `<?xml version="1.1" encoding='UTF-16'?>`。
- asEndElement→CCE。
