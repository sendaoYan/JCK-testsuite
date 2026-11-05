# javax.xml.stream.events.StartDocument — 设计案

## 1. API 功能
文档开始事件接口（extends XMLEvent）。方法：getSystemId、getCharacterEncodingScheme、encodingSet、
isStandalone、standaloneSet、getVersion。（无 getEncoding —— 那是 XMLStreamReader 方法。）

## 2. Java 用户如何使用
工厂 createStartDocument()/（encoding)/（encoding,version)/（encoding,version,standalone)；
解析：XMLEventReader 的第一个事件恒为 START_DOCUMENT(7)。

## 3. 开源代码实际用法调研
JDK 实现 StartDocumentEvent；JDK test/jdk javax/xml/stream；本仓库 StartDocumentTest。

## 4. 测试用例设计 + 易错细节（契约深化，testDeep()）
- 解析 XMLDecl 形态矩阵（Reader 输入）：decl 有 standalone=yes → sa=T,saSet=T；standalone=no → sa=F,saSet=T；
  无 standalone → **JDK-8 实现报 saSet=T（与规范不符）；JDK-27 报规范的 false —— 双侧差异各自锁定**
  （测试用 SA_SET_WHEN_ABSENT 常量参数化）。encoding 出现在 decl → encodingSet=T，否则 F；完全无 decl →
  version=null、encSet=F。
- Reader 输入 getCharacterEncodingScheme()=null（即使 decl 声明 encoding）；InputStream 输入返回实际编码
  "UTF-8"；StreamSource 的 systemId 透传到 getSystemId()。
- 工厂形态矩阵：无参默认 encoding="UTF-8"/version="1.0"/sa=T/saSet=F/encSet=T/systemId=""（空串）；
  (enc,ver,bool) 置 sa 与 saSet；(enc,ver) saSet=F；(enc) 默认 version 1.0。
- 编码形态 `<?xml version="1.1" encoding='UTF-16'?>`（version 双引号、encoding 单引号）全等。
- 错误窄化 asEndElement → CCE。
