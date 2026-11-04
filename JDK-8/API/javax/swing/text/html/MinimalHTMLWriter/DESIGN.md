# javax.swing.text.html.MinimalHTMLWriter — DESIGN

## 1. API 功能
把任意 `StyledDocument` 导出为"最小"HTML（`extends AbstractWriter`）。构造器 `MinimalHTMLWriter(Writer, StyledDocument)` 与 `(Writer, StyledDocument, int pos, int len)`。核心 `write()` 输出 `<html><head><style>`、`<body>`，按样式映射（bold/italic/underline）生成 `<b>/<i>/<u>` 标签；protected 方法 `writeHeader/writeBody/writeStartParagraph/writeLeaf/writeHTMLTags` 等负责分片。`HTMLEditorKit.write` 对 StyledDocument 委托给它。

## 2. Java 用户如何使用
构造 `DefaultStyledDocument`，`insertString` 文本并用 `setCharacterAttributes` 设样式后，`new MinimalHTMLWriter(new StringWriter(), doc).write()` 得到带样式标签的 HTML；测试断言 contains 核心 token 不做全等。

## 3. 开源代码实际用法调研（来源：JDK 源码）
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/html/HTMLEditorKit.java`：`write()` 内 `MinimalHTMLWriter w = new MinimalHTMLWriter(out, (StyledDocument)doc, pos, len); w.write();`。
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/html/MinimalHTMLWriter.java`：`setFontMask` 依据 `StyleConstants.Bold/Italic/Underline` 生成 start/end 标签。

## 4. 测试用例设计（黑盒，`MinimalHTMLWriterTest`：styledDoc + token/range/negative，JDK-8 22 断言）
1. styledDoc（DefaultStyledDocument "styled hello" + bold 字符样式）：write 输出含 `<html`/`<body`/`<p`/`<b>`/"styled hello"/`<style`，且无 `<font`（最小导出不写 font 标签）。
2. range：4 参构造器范围导出仍含 `<html` 与范围内文本。
3. 负例（精确异常）：ctor(null Writer) OK 但 write NPE；ctor(null doc) 构造时 NPE。
