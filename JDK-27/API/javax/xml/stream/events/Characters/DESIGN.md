# javax.xml.stream.events.Characters — 设计案

## 1. API 功能
javax.xml.stream.events.Characters 接口。实例/声明方法：getData, isWhiteSpace, isCData, isIgnorableWhiteSpace。

## 2. Java 用户如何使用
匿名实现 `new javax.xml.stream.events.Characters() {...}` 逐方法调用；断言常量精确值。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.events.Characters）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 CharactersTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`CharactersTest.testBlackBox()`）覆盖：匿名实现实例化、常量精确值、每个抽象方法固定返回值断言。

## 5. 契约深化条目（2026-11，testDeep()）
- 工厂三态矩阵 plain(F,F,F)/cdata(T,F,F)/space(F,T,F)/ignorable(F,T,T)/空串/createCharacters("   ")ws=T；
  createSpace 码仍 4；createCharacters(null) 容忍 getData null；CDATA 编码 `<![CDATA[<&]]>]]>`。
- 解析三态：普通(F,F,F)；子元素间空白 4+ws=T+ig=F；CDATA 文本保留但 isCData()=false（impl quirk）。
- replacing=true 下 t1&amp;t2&#65; → [t1][&][t2][A] 四事件；IS_COALESCING=true 合并为 "t1&t2<c>z" 单事件。
