# javax.swing.text.html.HTMLEditorKit.Parser — DESIGN

## 1. API 功能
HTML 解析器的抽象契约类（protected 无参构造器，abstract），唯一抽象方法 `parse(Reader r, ParserCallback cb, boolean ignoreCharSet) throws IOException`：读取输入并回调 `ParserCallback` 的各 handle 方法。JDK 实际实现是 `javax.swing.text.html.parser.ParserDelegator`；`HTMLEditorKit.getParser()`（protected）返回该共享实例，`read()/insertHTML()` 用它解析。

## 2. Java 用户如何使用
实现 `Parser` 子类（自定义解析）或经 `HTMLEditorKit` 子类暴露 `getParser()` 获取 `ParserDelegator`，然后 `parser.parse(new StringReader(html), callback, false); callback.flush();` 驱动解析。用户侧通常不直接触碰，而是通过 `HTMLEditorKit.read` 间接使用。

## 3. 开源代码实际用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLEditorKit.java`：`protected Parser getParser() { if (defaultParser == null) defaultParser = new ParserDelegator(); return defaultParser; }`；`ensureParser(doc)` 用 `doc.getParser()` 或 `getParser()`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/parser/ParserDelegator.java`：`extends HTMLEditorKit.Parser` 并实现 `parse`。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/html/parser/...`：大量用例直接构造 `ParserDelegator` 或经 `HTMLEditorKit` 验证解析行为（如 bug7165725、HtmlCommentTagParseTest）。

## 4. 测试用例设计（黑盒部分，`ParserTest.testBlackBox`）
1. 具体子类 `RecordingParser` 实现 `parse` 可实例化、可调用，且 `parseCalls==1`、`ignoreCharset` 标志透传。
2. 经 `HTMLEditorKit` 子类暴露 `getParser()`：真实 parser 非空且 `instanceof HTMLEditorKit.Parser`。
3. 真实 parser `parse(...)` + `callback.flush()` 正常解析不抛异常。
