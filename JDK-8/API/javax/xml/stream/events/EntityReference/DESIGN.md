# javax.xml.stream.events.EntityReference — 设计案

## 1. API 功能
实体引用事件接口（extends XMLEvent）。方法：getName、getDeclaration（EntityDeclaration）。事件码 9。

## 2. Java 用户如何使用
工厂 createEntityReference(name, EntityDeclaration)；解析：IS_REPLACING_ENTITY_REFERENCES=false 时
内部一般实体引用产生 ENTITY_REFERENCE 事件；默认 true 时被替换为字符。

## 3. 开源代码实际用法调研
JDK 实现 EntityReferenceEvent；JDK test/jdk javax/xml/stream；本仓库 EntityReferenceTest。

## 4. 测试用例设计 + 易错细节（契约深化，testDeep()）
- 工厂：name 保留；null declaration 容忍（getDeclaration()=null）；事件码 9 + isEntityReference；
  编码 `&foo;` 全等；createEntityReference(null,null) 容忍。
- 解析（replacing=false）：`<!DOCTYPE r [<!ENTITY e1 'Hello'>]><r>&e1;</r>` → ER 事件 name=e1、
  getDeclaration() 非 null 且 getName=e1、getReplacementText="Hello"（声明与引用闭环）。
- 解析（replacing=true 默认）：无 ER 事件，替换为 CHARACTERS "Hello"。
- 负例：未解析实体（NDATA）出现在内容中 → XMLStreamException "not permitted"。
- 错误窄化 asStartElement → CCE。
