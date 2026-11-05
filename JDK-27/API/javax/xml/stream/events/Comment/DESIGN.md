# javax.xml.stream.events.Comment — 设计案

## 1. API 功能
javax.xml.stream.events.Comment 接口。实例/声明方法：getText。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.stream.events.Comment() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.events.Comment）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 CommentTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`CommentTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 5. 契约深化条目（2026-11，testDeep()）
- 工厂：getText 全等、码 5、isXxx 族全 false、编码 `<!-- note -->`、内容 <>&"' 不转义、createComment(null) 容忍。
- 解析：序言注释在 START_DOCUMENT 之后（首事件恒为 SD，易错）；尾声注释；元素内 a<!--mid-->b 保序。
- 闭环：createComment→XMLEventWriter 输出含 `<!--c1-->`；asCharacters→CCE。
