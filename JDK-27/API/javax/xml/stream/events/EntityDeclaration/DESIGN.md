# javax.xml.stream.events.EntityDeclaration — 设计案

## 1. API 功能
javax.xml.stream.events.EntityDeclaration 接口。实例/声明方法：getPublicId, getSystemId, getName, getNotationName, getReplacementText, getBaseURI。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.stream.events.EntityDeclaration() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.events.EntityDeclaration）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 EntityDeclarationTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`EntityDeclarationTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 5. 契约深化条目（2026-11，testDeep()）
- 三形态矩阵（经 DTD.getEntities()）：内部（replacement="Hello"、id 全 null）；外部 PUBLIC+NDATA（pub/sys/not 精确）；
  外部 SYSTEM（仅 systemId）。码 15、baseURI null。
- 编码 `<!ENTITY e1 "Hello">` 全等；工厂 createDTD getEntities()=null。
- getEntities() 顺序为 map 装载序（实测 b,a 回读 a,b）——断言集合不断言序。
