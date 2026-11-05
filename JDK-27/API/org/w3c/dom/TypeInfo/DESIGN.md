# org.w3c.dom.TypeInfo — 设计案

## 1. API 功能
org.w3c.dom.TypeInfo 接口。常量：DERIVATION_RESTRICTION, DERIVATION_EXTENSION, DERIVATION_UNION, DERIVATION_LIST（共 4 个）。 实例/声明方法：getTypeName, getTypeNamespace, isDerivedFrom。

## 2. Java 用户如何使用
匿名实现 `new org.w3c.dom.TypeInfo() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（org.w3c.dom.TypeInfo）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 TypeInfoTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`TypeInfoTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 深化第 2 轮（契约级，2026-11-05）
DTD 侧真实值：`<!ATTLIST item id ID #IMPLIED>` → attr TypeInfo typeName="ID"、typeNamespace="http://www.w3.org/TR/REC-xml"；Xerces DTD **不填** element typeName（null）；无 DTD 时 getSchemaTypeInfo() 仍非 null 但 typeName/typeNamespace 均 null。DERIVATION_RESTRICTION=1/EXTENSION=2/UNION=4/LIST=8；isDerivedFrom 对无关/空基型恒 false。
