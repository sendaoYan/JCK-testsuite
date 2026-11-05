# javax.xml.stream.events.DTD — 设计案

## 1. API 功能
DTD 事件接口（extends XMLEvent）。方法：getDocumentTypeDeclaration（完整 <!DOCTYPE ...> 串）、
getProcessedDTD（Object，JDK 返回 null）、getEntities（List<EntityDeclaration>）、
getNotations（List<NotationDeclaration>）。事件码 DTD=11。

## 2. Java 用户如何使用
工厂 createDTD(String)（字符串原样保留、不解析）；解析：带 DOCTYPE 的文档在 START_DOCUMENT 后产生 DTD 事件。

## 3. 开源代码实际用法调研
JDK 实现 DTDEvent；JDK test/jdk javax/xml/stream；本仓库 DTDTest。

## 4. 测试用例设计 + 易错细节（契约深化，testDeep()）
- 完整内部子集：getDocumentTypeDeclaration() 与原文全等（含换行、引号形态）；getEntities/getNotations
  按声明列出（实测 e1,e2 / bin,gif 顺序稳定）；getProcessedDTD()=null。
- 空内部子集 `<!DOCTYPE r >`：声明串全等、entities/notations 均 null。
- 无 DOCTYPE 文档：不产生 DTD 事件。
- 工厂 createDTD：串 round-trip 全等；entities/notations/processed 全 null（不解析内部子集）；事件码 11；
  编码输出 = 输入串全等。
- 易错：getProcessedDTD 无 processAny 之类方法面（javap 实测仅四方法）；DTD 事件码是 11 不是 10。
