# javax.xml.stream.events.NotationDeclaration — 设计案

## 1. API 功能
记法声明接口（extends XMLEvent）。方法：getName、getPublicId、getSystemId。事件码 NOTATION_DECLARATION=14。

## 2. Java 用户如何使用
解析含 `<!NOTATION>` 内部子集的文档后从 DTD.getNotations() 列表取得；无公开工厂构造。

## 3. 开源代码实际用法调研
JDK 实现 NotationDeclarationImpl；JDK test/jdk javax/xml/stream；本仓库 NotationDeclarationTest。

## 4. 测试用例设计 + 易错细节（契约深化，testDeep()）
- PUBLIC 形态 `<!NOTATION gif PUBLIC '-//gif//gif'>` → publicId 精确、systemId null；
  SYSTEM 形态 `<!NOTATION bin SYSTEM 'binfmt'>` → systemId 精确、publicId null。事件码 14。
- 编码：`<!NOTATION gif PUBLIC "-//gif//gif">`（输出双引号）全等。
- 记法只经 DTD.getNotations() 可达：reader 从不产出独立的码 14 事件。
- 无记法文档（只有实体）→ getNotations()=null 而 getEntities() 非空 —— 两列表独立。
