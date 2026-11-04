# javax.swing.text.html.HTMLWriter — DESIGN

## 1. API 功能
把 `HTMLDocument` 序列化回 HTML 文本的书写器（`extends AbstractWriter`）。构造器 `HTMLWriter(Writer, HTMLDocument)`（整篇）与 `HTMLWriter(Writer, HTMLDocument, int pos, int len)`（范围）。核心 `write()` 遍历元素输出 `<html>/<head>/<body>`、块级/内联标签、属性、样式与注释；protected 方法 `startTag/endTag/emptyTag/text/comment/writeAttributes` 负责分片输出。`HTMLEditorKit.write` 对 HTMLDocument 委托给它。

## 2. Java 用户如何使用
`kit.read(...)` 填充 HTMLDocument 后 `new HTMLWriter(new StringWriter(), doc).write()` 得到 HTML 文本；或 4 参构造器只导出 [pos, pos+len) 范围。测试断言输出 contains 核心 token（标签/属性/实体重编码），不做全等。

## 3. 开源代码实际用法调研（来源：JDK 源码）
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/html/HTMLEditorKit.java`：`write()` 内 `HTMLWriter w = new HTMLWriter(out, (HTMLDocument)doc, pos, len); w.write();`。
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/html/HTMLWriter.java`：`write()` 遍历 `ElementIterator(doc.getDefaultRootElement())`，实体经 `output` 重编码。

## 4. 测试用例设计（黑盒，`HTMLWriterTest`：testTokenOutput/testRangeWriter/testNegatives，JDK-8 33 断言）
1. testTokenOutput（固定 RICH_HTML 文档）：输出含 `<html`/`<body`/`<div`/`<p`/`<ul`/`<li`/`<img`/`<form`/`<input`/`align="center"`/`x.png`/ALT 值/按钮文本；实体重编码 `&amp;`/`&lt;` 且无裸 `&`。
2. 空文档：仅 skeleton（`<html`/`<body`）无正文标签。
3. testRangeWriter：4 参构造器范围导出仍含 `<html` 与范围内文本。
4. testNegatives（精确异常）：ctor(null Writer) OK 但 write NPE；ctor(null doc) 构造时 NPE。
