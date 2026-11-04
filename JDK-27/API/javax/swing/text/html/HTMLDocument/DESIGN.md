# javax.swing.text.html.HTMLDocument — DESIGN

## 1. API 功能
HTML 文档模型（`extends DefaultStyledDocument`，非抽象，无接口）。核心方法分组：
- 构造器：`HTMLDocument()`、`HTMLDocument(Content, StyleSheet)`、`HTMLDocument(StyleSheet)`。
- 解析：由 `HTMLEditorKit.read(reader, doc, pos)` 填充。
- 结构查询：`getDefaultRootElement()`、`getParagraphElement(int)`、`getElement(String id)`、`getElement(Element, Attribute, Object)`、`getIterator(HTML.Tag)`。
- 编辑：`insertAfterStart(Element, String)`、`insertBeforeEnd(Element, String)`。
- 配置/查询：`getTokenThreshold()/setTokenThreshold()`、`getPreservesUnknownTags()/setPreservesUnknownTags()`、`getBase()/setBase()`、`getStyleSheet()`、`getParser()`、`getReader(int)`。

## 2. Java 用户如何使用
`HTMLEditorKit kit = new HTMLEditorKit(); HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument(); kit.read(new StringReader(html), doc, 0);` 后：`doc.getDefaultRootElement()` 取根元素、`doc.getParagraphElement(off)` 取段落、`doc.getElement("id")` 按 id 查元素、`doc.insertAfterStart(el, "<b>..</b>")` 插内容、`doc.setBase(new URL(...))` 设基准。典型链：parse → getDefaultRootElement → getElement(id) → insertAfterStart。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLDocument.java:575`：`public Iterator getIterator(HTML.Tag t)` 返回叶子迭代器（块级标签返回 null）。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLDocument.java:3700`：`option = new Option(attr);` 解析表单 option 时构造 Option。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLEditorKit.java`：`createDefaultDocument()` 返回 HTMLDocument 实例。

## 4. 测试用例设计（黑盒部分，`HTMLDocumentTest.testBlackBox`）
1. 三个构造器均非 null。
2. 解析 `<p>hello</p>`：长度 6、文本含 "hello"、root 名 "html" 且为 BlockElement、root 有 head+body 2 个子元素。
3. getParagraphElement(1) 名 "p" 且为 BlockElement。
4. getElement("p1") 按 id 查；getElement(root, ID, "p1")；未知 id 与 null 均返回 null；getElement(root, NameAttribute, BODY) 按名查 body。
5. 对 body 元素 insertAfterStart("<b>B</b>") 后文本含 "Bhello"；再对（重新取到的）body insertBeforeEnd("<p>X</p>") 后文本仍含 "Bhello" 且新增 "X"。
6. tokenThreshold 默认 100、set 10 往返；preservesUnknownTags 默认 true、set false 往返。
7. base 默认 null、setBase 往返；getStyleSheet() instanceof StyleSheet；getParser() 非 null；getReader(1)（body 内偏移）返回 HTMLReader。
