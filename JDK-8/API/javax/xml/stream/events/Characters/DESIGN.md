# javax.xml.stream.events.Characters — 设计案

## 1. API 功能
字符数据事件接口（extends XMLEvent）。方法：getData、isCData、isWhiteSpace、isIgnorableWhiteSpace。

## 2. Java 用户如何使用
工厂：createCharacters/createCData/createSpace/createIgnorableSpace（四形态三态矩阵）；
解析：XMLEventReader 对文本/CDATA/空白产出 CHARACTERS(4) 事件；IS_COALESCING=true 时合并。

## 3. 开源代码实际用法调研
JDK 实现 CharacterEvent；JDK test/jdk javax/xml/stream（含 coalescing 回归）；本仓库 CharactersTest。

## 4. 测试用例设计 + 易错细节（契约深化，testDeep()）
- 工厂三态矩阵：plain(F,F,F)、createCData(T,F,F)、createSpace(F,T,F)、createIgnorableSpace(F,T,T)、
  空串 getData("") 非空白、createCharacters("   ") isWhiteSpace=T。createSpace 事件码仍是 4（不是 SPACE=6）。
- createCharacters(null) 被容忍，getData()=null；CDATA 内容字符不转义，编码输出 `<![CDATA[<&]]>]]>`。
- 解析三态：普通文本(F,F,F)；子元素间空白 CHARACTERS(4)+ws=T+ig=F（无 DTD 不产生 ignorable/SPACE）；
  CDATA 节文本保留但 isCData()=false（JDK 默认分配器不保留 CDATA 标志 —— 双侧一致的 impl quirk，真 T 只有工厂路径）。
- 默认（replacing=true）下预定义实体/字符引用切分文本：t1&amp;t2&#65; → [t1][&][t2][A] 四个事件；
  IS_COALESCING=true 合并实体+CDATA+文本为单事件 "t1&t2<c>z"（isCData 仍 false）。
- 错误窄化 asEndElement/asStartElement → CCE。
