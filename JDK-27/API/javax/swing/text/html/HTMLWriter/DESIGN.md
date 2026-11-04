# javax.swing.text.html.HTMLWriter — DESIGN

## 1. API 功能
把 `HTMLDocument` 序列化回 HTML 文本的书写器（`extends AbstractWriter`）。构造器 `HTMLWriter(Writer, HTMLDocument)`（写整篇）与 `HTMLWriter(Writer, HTMLDocument, int pos, int len)`（写范围）。核心 `write()` 遍历文档元素，输出 `<html>/<head>/<body>`、块级/内联标签、属性、样式与注释；protected 方法 `startTag/endTag/emptyTag/text/comment/writeAttributes` 等负责分片输出。`HTMLEditorKit.write` 对 `HTMLDocument` 委托给它。

## 2. Java 用户如何使用
`HTMLEditorKit kit = new HTMLEditorKit(); HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument(); kit.read(new StringReader(html), doc, 0);` 后 `new HTMLWriter(new StringWriter(), doc).write()` 得到 HTML 文本；或 `new HTMLWriter(w, doc, 0, doc.getLength()).write()` 只导出范围。

## 3. 开源代码实际用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLEditorKit.java`：`write()` 内 `HTMLWriter w = new HTMLWriter(out, (HTMLDocument)doc, pos, len); w.write();`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLWriter.java`：`write()` 遍历 `ElementIterator(doc.getDefaultRootElement())`，`startTag/endTag/text/emptyTag` 输出元素与文本。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/html/7189299/bug7189299.java` 等用例间接经 `JEditorPane` 的 HTML 内容覆盖书写器路径。

## 4. 测试用例设计（黑盒部分，`HTMLWriterTest.testBlackBox`）
1. 2 参构造器 write 输出非空且含 `<html>`、`<body>`、`<b>bold</b>`。
2. 4 参构造器按范围 write 仍含正文文本与 `<html>`。
