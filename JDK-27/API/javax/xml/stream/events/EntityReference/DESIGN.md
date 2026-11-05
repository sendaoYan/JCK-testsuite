# javax.xml.stream.events.EntityReference — 设计案

## 1. API 功能
javax.xml.stream.events.EntityReference 接口。实例/声明方法：getDeclaration, getName。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.stream.events.EntityReference() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.events.EntityReference）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 EntityReferenceTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`EntityReferenceTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 5. 契约深化条目（2026-11，testDeep()）
- 工厂：name 保留、null declaration 容忍、码 9 + isEntityReference、编码 `&foo;`、(null,null) 容忍。
- 解析（replacing=false）：`&e1;` → ER 事件 name=e1 + getDeclaration().getReplacementText()="Hello"（声明闭环）。
- 解析（replacing=true 默认）：无 ER 事件、替换为 CHARACTERS "Hello"。
- 负例：NDATA 未解析实体入内容 → XMLStreamException "not permitted"；asStartElement→CCE。
