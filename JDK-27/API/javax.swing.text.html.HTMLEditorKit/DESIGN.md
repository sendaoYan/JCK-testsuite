# javax.swing.text.html.HTMLEditorKit — DESIGN

## 1. API 功能
Swing 的 HTML 文本编辑工具包（`extends StyledEditorKit`，implements `Accessible`），为 `JEditorPane`/`JTextPane` 提供 `text/html` 内容支持。方法分组：
- 类型/工厂：`getContentType()`（返回 `"text/html"`）、`getViewFactory()`（共享 `HTMLFactory`）、`createDefaultDocument()`（返回 `HTMLDocument`）、`getParser()`（protected，`ParserDelegator`）。
- 解析/序列化：`read(Reader,Document,int)`（把 HTML 解析进 `HTMLDocument`）、`write(Writer,Document,int,int)`（`HTMLDocument`→`HTMLWriter`，`StyledDocument`→`MinimalHTMLWriter`）、`insertHTML(...)`。
- 样式/属性：`getStyleSheet()/setStyleSheet(...)`、`getInputAttributes()`。
- 光标：`getDefaultCursor()/setDefaultCursor(...)`（默认 DEFAULT_CURSOR）、`getLinkCursor()/setLinkCursor(...)`（默认 HAND_CURSOR）。
- 动作：`getActions()`（组合默认 HTML 动作）、`clone()`；表单自动提交开关 `isAutoFormSubmission()/setAutoFormSubmission(...)`。

## 2. Java 用户如何使用
`new HTMLEditorKit()` → `jep.setEditorKit(kit)`（或 `new JEditorPane("text/html", html)`）→ `kit.createDefaultDocument()` 得到 `HTMLDocument` → `kit.read(new StringReader(html), doc, 0)` 解析 → `kit.write(new StringWriter(), doc, 0, len)` 序列化回 HTML；`kit.getStyleSheet()` 取默认样式表；`kit.getActions()` 取内置编辑动作；`kit.getDefaultCursor()/getLinkCursor()` 配置超链接光标。

## 3. 开源代码实际用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLEditorKit.java`：`createDefaultDocument()` 内 `HTMLDocument doc = new HTMLDocument(ss); doc.setParser(getParser());`；`write()` 内 `HTMLWriter w = new HTMLWriter(out, (HTMLDocument)doc, pos, len); w.write();` 与 `MinimalHTMLWriter w = new MinimalHTMLWriter(out, (StyledDocument)doc, pos, len);`。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/html/HTMLEditorKit/bug4102068.java`：`ek.setDefaultCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)); Cursor ct = ek.getDefaultCursor(); ek.setLinkCursor(...HAND_CURSOR); Cursor cl = ek.getLinkCursor();` 并断言 `ct.getType()==DEFAULT_CURSOR && cl.getType()==HAND_CURSOR`。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/html/HTMLEditorKit/4242228/bug4242228.java`：`htmlEditor.setEditorKit(new HTMLEditorKit());` 验证 setText 不抛异常。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/html/7189299/bug7189299.java`：`html = new JEditorPane("text/html", "<html><body><form .../></body></html>"); HTMLEditorKit htmlEditorKit = (HTMLEditorKit) html.getEditorKit(); ... htmlEditorKit.getInputAttributes();`。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/html/bug4839739.java`：`jep.setEditorKit(new HTMLEditorKit()); jep.setEditable(false); jep.setText(html);`。

## 4. 测试用例设计（黑盒部分，`HTMLEditorKitTest.testBlackBox`）
1. contentType == "text/html"；createDefaultDocument 返回 HTMLDocument；getViewFactory 为 HTMLFactory。
2. getStyleSheet 非空；getDefaultCursor 类型 DEFAULT_CURSOR、getLinkCursor 类型 HAND_CURSOR。
3. read 组合：解析 `<html><body><b>bold</b></body></html>` 后 getText 包含 "bold" 且非空。
4. write 组合：序列化输出包含 `<html>` 与 `<b>bold</b>`。
5. getActions 非空且含 HTMLTextAction 实例；getInputAttributes 非空。
6. isAutoFormSubmission 默认 true、set false/true 往返；setStyleSheet/getStyleSheet 往返；set/getDefaultCursor、set/getLinkCursor 往返。
7. clone 返回不同实例且保持 contentType。
