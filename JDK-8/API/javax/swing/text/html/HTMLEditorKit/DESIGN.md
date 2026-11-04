# javax.swing.text.html.HTMLEditorKit — DESIGN

## 1. API 功能
Swing 的 HTML 文本编辑工具包（`extends StyledEditorKit`，implements `Accessible`），为 `JEditorPane`/`JTextPane` 提供 `text/html` 支持。方法分组：
- 类型/工厂：`getContentType()`（"text/html"）、`getViewFactory()`（共享 HTMLFactory）、`createDefaultDocument()`（HTMLDocument）、`getParser()`（protected ParserDelegator）。
- 解析/序列化：`read(Reader,Document,int)`、`write(Writer,Document,int,int)`（HTMLDocument→HTMLWriter、StyledDocument→MinimalHTMLWriter）、`insertHTML(...)`。
- 样式：`getStyleSheet()/setStyleSheet(...)`、`getInputAttributes()`。
- 光标：`getDefaultCursor()`（DEFAULT_CURSOR）/`getLinkCursor()`（HAND_CURSOR）。
- 动作：`getActions()`、`clone()`；`isAutoFormSubmission()/setAutoFormSubmission(...)`。

## 2. Java 用户如何使用
`new HTMLEditorKit()` → `jep.setEditorKit(kit)` → `kit.createDefaultDocument()` → `kit.read(new StringReader(html), doc, 0)` 解析 → `kit.write(new StringWriter(), doc, 0, len)` 序列化；`kit.getActions()` 取内置动作（HTML-only 动作集两侧完全一致）。

## 3. 开源代码实际用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/html/HTMLEditorKit.java`：`createDefaultDocument()` 内 `HTMLDocument doc = new HTMLDocument(ss); doc.setParser(getParser());`；`write()` 委托 HTMLWriter/MinimalHTMLWriter。
- `~/git/jdk8u/jdk/test/javax/swing/text/html/HTMLEditorKit/bug4102068.java`：光标 get/set 往返断言。
- `~/git/jdk8u/jdk/test/javax/swing/text/html/7189299/bug7189299.java`：`getInputAttributes()` 用法。

## 4. 测试用例设计（黑盒，`HTMLEditorKitTest`：testShape/testConstants/testActions/testLifecycle/testReadWriteContracts/testLinkController，JDK-8 143 断言）
1. testConstants：DEFAULT_CSS/BOLD_ACTION/COLOR_ACTION/FONT_CHANGE_BIGGER/PARA_INDENT_LEFT/IMG_ALIGN_TOP 与 `ParserCallback.IMPLIED == "_implied_"`。
2. testActions：`getActions()` 数量版本分支（JDK-8 85 / JDK-27 87，含 DefaultEditorKit 55/57）；30 个 HTML-only 动作名双侧完全一致（font-bold、font-size-1、InsertTable、activate-link-action 等全集锁死）。
3. testLifecycle：clone 独立实例、HTMLFactory 共享、StyleSheet 身份、光标默认值、autoFormSubmission 默认 true、install/deinstall JEditorPane 往返。
4. testReadWriteContracts（负例矩阵，精确异常）：read(null Reader) 的 NPE 被 parser 内部捕获仅打印（tolerated 断言）；write 非 HTMLDocument 组合的 BadLocationException/IOException；insertHTML 正例 `text.equals("\nonbolde\ntwo")`。
5. testLinkController：`LinkController` 继承 MouseAdapter、serialVersionUID 1338530902363040739 + 序列化往返。
