# javax.xml.stream.events.EntityDeclaration — 设计案

## 1. API 功能
实体声明接口（extends XMLEvent）。方法：getPublicId、getSystemId、getName、getNotationName、
getReplacementText、getBaseURI。事件码 ENTITY_DECLARATION=15。

## 2. Java 用户如何使用
解析含 DTD 内部子集的文档后从 DTD.getEntities() 列表取得；无公开工厂直接构造（createEntityReference 引用它）。

## 3. 开源代码实际用法调研
JDK 实现 EntityDeclarationImpl；JDK test/jdk javax/xml/stream；本仓库 EntityDeclarationTest。

## 4. 测试用例设计 + 易错细节（契约深化，testDeep()）
- 三形态矩阵（经 DTD.getEntities()）：内部实体 `<!ENTITY e1 'Hello'>` → replacement="Hello"、三 id 均 null、
  baseURI null；外部 PUBLIC+NDATA `<!ENTITY e2 PUBLIC 'pub1' 'sys1' NDATA gif>` → pub/sys/notation 精确、
  replacement null；外部 SYSTEM `<!ENTITY e3 SYSTEM 'sys3'>` → 仅 systemId。事件码均 15。
- 编码：`<!ENTITY e1 "Hello">`（输出用双引号）全等。
- 工厂 createDTD 的字符串不解析：getEntities()=null。
- 易错：getEntities() 的顺序是 map 装载序（实测 b,a 两声明回读为 a,b）——不断言声明序，只断言集合与计数。
