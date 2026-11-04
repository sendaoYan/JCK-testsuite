# javax.swing.text.html.HTMLDocument — DESIGN

## 1. API 功能
HTML 文档模型（`extends DefaultStyledDocument`，非抽象，无接口）。核心方法分组：
- 构造器：`HTMLDocument()`、`HTMLDocument(Content, StyleSheet)`、`HTMLDocument(StyleSheet)`。
- 结构查询：`getDefaultRootElement()`、`getParagraphElement(int)`、`getElement(String id)`、`getElement(Element, Attribute, Object)`、`getIterator(HTML.Tag)`。
- 编辑：`insertAfterStart/insertBeforeEnd/insertAfterEnd/insertBeforeStart(Element, String)`、`setInnerHTML/setOuterHTML(Element, String)`。
- 配置/查询：`getTokenThreshold()/setTokenThreshold()`、`getPreservesUnknownTags()`、`getBase()/setBase()`、`getStyleSheet()`、`getParser()`、`getReader(int)`、`processHTMLFrameHyperlinkEvent(...)`。

## 2. Java 用户如何使用
`HTMLEditorKit kit = new HTMLEditorKit(); HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument(); kit.read(new StringReader(html), doc, 0);` 后查询元素树、按 id 取元素、插入 HTML 片段、处理 frame 链接事件。惰性契约：fresh doc 的 getParser()==null、getTokenThreshold()==Integer.MAX_VALUE；经 kit.read 后分别变为 ParserDelegator 与 100。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/html/HTMLDocument.java`：`getIterator(HTML.Tag)` 块级标签返回 null、叶子标签返回非空迭代器；`getReader(pos)` 对 body 外位置抛 RuntimeException。
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/html/HTMLEditorKit.java`：`createDefaultDocument()` 返回 HTMLDocument 实例。

## 4. 测试用例设计（黑盒，`HTMLDocumentTest`：testShape/testConstructorsAndProperties/testTreeShape/testIterator/testReader/testInserts/testNegatives/testFrameProcessing，JDK-8 104 断言）
1. testTreeShape（固定 DOC_HTML）：文档长度 21、getText 精确等于 "  \nHello & bye\nSecond"、根/头/体精确偏移 [0,3)/[3,22)、p-implied 结构、title 的 ENDTAG 属性值是 String "true"、CR run 属性键 String "CR" 值 Boolean.TRUE。
2. testIterator：块标签 → null；缺席叶子标签 → 非空但 isValid()==false；IMG 迭代区间 [6,7)/[12,13) 精确断言。
3. testConstructorsAndProperties：3 构造器、sheet 身份、惰性 parser/threshold（fresh null/MAX_VALUE → read 后 ParserDelegator/100）、preservesUnknownTags true、base 往返。
4. testInserts：四插入方法组合后文本 "\nZXABY\n"（insertAfterEnd 的内容不入文本）、setInnerHTML/setOuterHTML 树变化、customtag 在 preservesUnknownTags 下的保留/丢弃。
5. testNegatives（精确异常）：getReader(pos) 非法位置 → RuntimeException "Must insert new content into body element-"；其余负例按实测宽容断言。
6. testFrameProcessing（回调序列样例）：processHTMLFrameHyperlinkEvent 匹配 frame 后 SRC 属性更新 + 恰 1 次 changedUpdate + 1 次 undoableEdit 回调（监听器计数断言）；不匹配 frame 时文档长度不变。
