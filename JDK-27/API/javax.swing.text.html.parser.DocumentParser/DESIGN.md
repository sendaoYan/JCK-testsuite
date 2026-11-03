# javax.swing.text.html.parser.DocumentParser — DESIGN

## 1. API 功能
HTML 文档解析器（`extends Parser`）。把 HTML 流解析后按 HTMLEditorKit 回调模型通知：隐含标签带 `IMPLIED` 属性，`handleSimpleTag` 处理空标签，`handleComment` 处理注释，末尾 `handleEndOfLineString`。
- 构造器：`DocumentParser(DTD dtd)`。
- 核心：`parse(Reader in, HTMLEditorKit.ParserCallback callback, boolean ignoreCharSet) throws IOException`。

## 2. Java 用户如何使用
`new DocumentParser(DTD.getDTD("html32"))`，传 `StringReader` 与自定义 `HTMLEditorKit.ParserCallback` 子类，覆写 `handleStartTag/handleText/handleSimpleTag/handleEndTag/handleComment/handleEndOfLineString`，驱动解析并捕获文档结构。

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/parser/ParserDelegator.java`（自用）：
  ```java
  public void parse(Reader r, HTMLEditorKit.ParserCallback cb, boolean ignoreCharSet) throws IOException {
      new DocumentParser(getDefaultDTD()).parse(r, cb, ignoreCharSet);
  }
  ```
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/html/HTMLEditorKit/bug4238223.java`：`super.parse(r, new TestCallback(), false)`，回调断言 `handleStartTag/handleText/handleComment` 的位置与内容。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/parser/DocumentParser.java`（自写文档）：`<p>blah` 会产生隐含 html/head/body + p + 文本 + 收尾回调。

## 4. 测试用例设计（黑盒部分，`DocumentParserTest.testBlackBox`）
1. `<p>Hello <b>world</b></p>`：start 序列 html/head/body/p/b、隐含标志（html/head/body true、p false）、end 标签集合、两个文本块、EOL=="\n"。
2. `<br>foo`：simple 标签 br、文本 foo。
3. `<!-- note -->`：注释回调含 note。
4. `ignoreCharSet=true`：仍正常产出 p 与文本、EOL 不变。
