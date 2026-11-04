# javax.swing.text.html.MinimalHTMLWriter — DESIGN

## 1. API 功能
把任意 `StyledDocument` 导出为“最小”HTML 文本（`extends AbstractWriter`），无需完整 HTMLDocument。构造器 `MinimalHTMLWriter(Writer, StyledDocument)` 与 `MinimalHTMLWriter(Writer, StyledDocument, int pos, int len)`。核心 `write()` 输出 `<html><head><style>`、`<body>`，按样式映射（bold/italic/underline 等）生成 `<b>/<i>/<u>` 或 `<font>/<span>` 标签；protected 方法 `writeHeader/writeBody/writeStartParagraph/writeLeaf/writeHTMLTags/startFontTag/endFontTag` 等负责分片。`HTMLEditorKit.write` 对 `StyledDocument` 委托给它。

## 2. Java 用户如何使用
构造 `DefaultStyledDocument`，`insertString` 文本并用 `setCharacterAttributes`/`setParagraphAttributes` 设样式后，`new MinimalHTMLWriter(new StringWriter(), styledDoc).write()` 得到带样式标签的 HTML 片段；或 `new MinimalHTMLWriter(w, doc, 0, len)` 只导出范围。

## 3. 开源代码实际用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLEditorKit.java`：`write()` 内 `MinimalHTMLWriter w = new MinimalHTMLWriter(out, (StyledDocument)doc, pos, len); w.write();`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/MinimalHTMLWriter.java`：`write()` 内 `writeHeader(); ... writeBody();`，`setFontMask` 依据 `StyleConstants.Bold/Italic/Underline` 生成 start/end 标签。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/rtf/RTFWriteParagraphAlignTest.java` 等文档书写器用例，覆盖 StyledDocument→Writer 的通用路径。

## 4. 测试用例设计（黑盒部分，`MinimalHTMLWriterTest.testBlackBox`）
1. 2 参构造器 write 输出非空且含 `<html>`、`<body>`、文本 "hello world"、`<b>`（bold 样式被表达）。
2. 4 参构造器按范围 write 仍含正文与 `<html>`。
