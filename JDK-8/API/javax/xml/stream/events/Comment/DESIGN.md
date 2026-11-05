# javax.xml.stream.events.Comment — 设计案

## 1. API 功能
注释事件接口（extends XMLEvent）。方法：getText（注释内文本，不含 <!-- -->）。

## 2. Java 用户如何使用
工厂 createComment(String)；解析：文档序言/元素内/尾声的注释均产生 COMMENT(5) 事件。

## 3. 开源代码实际用法调研
JDK 实现 CommentEvent；JDK test/jdk javax/xml/stream；本仓库 CommentTest。

## 4. 测试用例设计 + 易错细节（契约深化，testDeep()）
- 工厂：getText 全等；事件码 5；isXxx 族全 false；编码 `<!-- note -->` 全等；注释内 <>&"' 不转义；
  createComment(null) 容忍（getText null）。
- 解析：`<!--c1--><r>x</r><!--tail-->` —— nextEvent 第一个总是 START_DOCUMENT，之后才是序言注释；
  尾声注释在 END_DOCUMENT 之前；`<r>a<!--mid-->b</r>` 元素内注释保持文本序 a/注释/b。
- 闭环：createComment("c1") → XMLEventWriter.add → 输出含 `<!--c1-->`。
- 错误窄化 asCharacters → CCE。
