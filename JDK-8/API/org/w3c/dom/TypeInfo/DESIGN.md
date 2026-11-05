# TypeInfo — 设计案

## 1. API 功能
（见 org.w3c.dom.TypeInfo javadoc / DOM Level 3 Core）

## 2. 使用方式
经 Document/DOMConfiguration/LSParser 组合取得实例或回调。

## 3. 参考来源
W3C DOM Level 3 Core/LS 规范；Oracle javadoc；OpenJDK Xerces 实测（双侧探针）。

## 4. 测试用例设计 + 易错规范细节
## 深化第 2 轮（契约级，2026-11-05）
DTD 侧真实值：`<!ATTLIST item id ID #IMPLIED>` → attr TypeInfo typeName="ID"、typeNamespace="http://www.w3.org/TR/REC-xml"；Xerces DTD **不填** element typeName（null）；无 DTD 时 getSchemaTypeInfo() 仍非 null 但 typeName/typeNamespace 均 null。DERIVATION_RESTRICTION=1/EXTENSION=2/UNION=4/LIST=8；isDerivedFrom 对无关/空基型恒 false。
