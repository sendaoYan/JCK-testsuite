# javax.xml.stream.XMLStreamWriter — 设计案（JDK-8）

## 1. API 功能
拉模型写接口（writeStartElement/writeEmptyElement/writeEndElement/writeEndDocument 结构，writeAttribute/writeNamespace/writeDefaultNamespace 声明，writeCharacters/writeCData/writeComment/writeProcessingInstruction/writeDTD/writeEntityRef 内容，writeStartDocument×3，setPrefix/setDefaultNamespace/setNamespaceContext/getPrefix/getNamespaceContext，flush/close/getProperty）。

## 2. 使用方式
XMLOutputFactory.createXMLStreamWriter(...) 获得；顺序写出声明→元素→内容；flush/close 落盘。

## 3. 参考来源
- Oracle javadoc javax.xml.stream（StAX 1.2/JAXP；异常契约、事件常量、属性名常量）。
- JDK 内置实现 com.sun.xml.internal.stream.*（本 JDK 即被测实现）；OpenJDK jdk8u test/jdk/javax/xml 下 StAX 用例思路借鉴（代码自写）。
- javap（/data/yansendao/JDK/binary/temurin/jdk8u372-b07/bin/javap）枚举公开方法面；行为断言全部以本机 JDK 实测为准。

## 4. 测试用例设计 + 易错细节
### 核心闭环：写→读 round-trip
writeStartDocument/Comment/PI/StartElement+Namespace+DefaultNamespace+Attribute×2/Characters(转义)/CData/Characters/EmptyElement/EndElement/EndDocument → flush → **输出串逐字符锁定**（`<?xml version="1.0" encoding="UTF-8"?><!--c--><?pi data?><root xmlns:p="urn:p" xmlns="urn:d" p:k="v1" plain="v2">a&lt;b&gt;&amp;c"d'<![CDATA[raw <c>]]>z<p:e/></root>`）→ XMLInputFactory 回读树/文本/属性全等。转义规则锁定：`< > &` 转义、`" '` 不转义。
### startdoc 形态与输出通道
- writeStartDocument() → `<?xml version="1.0" ?>`（**注意空格**）；("1.0") → `<?xml version="1.0"?>`（无空格）；("ISO-8859-1","1.1") → 带编码版本串；OutputStream 默认通道 UTF-8；UTF-16 通道输出 BOM `fe ff`。
- writeDTD 原样、writeEntityRef("amp") → `&amp;`、writeProcessingInstruction("solo") → `<?solo?>`、writeEmptyElement → `<e/>`、writeCharacters(char[],1,2) → 切片输出。
### 前缀与未闭合
- setPrefix/getPrefix 往返；setDefaultNamespace 后 getPrefix(ns)==""。
- **易错**：writeEndDocument 会**自动闭合**未闭合元素（`<a><b></b></a>`）；close() 不闭合、不抛异常、输出悬空 `<a>x`。
- writeAttribute(未声明 ns)（非修复模式）→ XMLStreamException "Prefix cannot be null"。
### Quirk 锁定（两版本一致）
- writeCData("x]]>y") → 字面 `<![CDATA[x]]>y]]>`（**产生非法 XML，不拆分**）；writeComment("a--b") → 字面 `<!--a--b-->`。
- writer.getProperty(未知名) → IllegalArgumentException（与读侧 reader 返回 null 相反）。
- setNamespaceContext 接受且不影响非修复输出；重复 writeStartDocument 输出两份声明。

### 断言计数
见 XMLStreamWriterTest.java 末尾 PASS 输出（本轮深化后 50 断言，双侧只增不减）。
