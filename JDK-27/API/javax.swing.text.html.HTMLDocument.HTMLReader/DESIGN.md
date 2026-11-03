# javax.swing.text.html.HTMLDocument.HTMLReader — DESIGN

## 1. API 功能
HTML 解析回调（`HTMLDocument` 的嵌套 public 类，`extends HTMLEditorKit.ParserCallback`，非抽象，无接口）。核心方法分组：
- 构造：`doc.new HTMLReader(int offset)`、`doc.new HTMLReader(int popDepth, int pushDepth, int insertTag, HTML.Tag parentTag)`。
- 解析回调（继承/覆写 ParserCallback）：`handleStartTag(HTML.Tag, MutableAttributeSet, int)`、`handleText(char[], int)`、`handleEndTag(HTML.Tag, int)`、`handleSimpleTag`、`handleComment`、`handleEndOfLineString`、`flush()`。

## 2. Java 用户如何使用
通常由 `HTMLDocument.getReader(0)` 取得（HTMLEditorKit 解析时内部驱动）。手动驱动时：`HTMLDocument.HTMLReader r = doc.new HTMLReader(0);` 依次调 `handleStartTag(P, attrs, 0)` → `handleText("hello".toCharArray(), 0)` → `handleEndTag(P, 0)` → `flush()`，把内容写入 doc。典型链：start → text → end → flush → `doc.getText(...)` 验证。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLDocument.java:351`：`public HTMLEditorKit.ParserCallback getReader(int pos)` 返回 HTMLReader 供解析器回调。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLDocument.java:2435-2441`：HTMLReader 构造里 `new TagAction()/BlockAction()/ParagraphAction()/CharacterAction()/SpecialAction()/FormAction()/HiddenAction()` 并注册 tagMap。

## 4. 测试用例设计（黑盒部分，`HTMLReaderTest.testBlackBox`）
1. `doc.new HTMLReader(0)` 非 null 且 instanceof ParserCallback。
2. handleStartTag(P)→handleText("hello")→handleEndTag(P)→flush 后 doc 文本含 "hello"。
3. `doc.new HTMLReader(0,0,0,BODY)` 5 参构造非 null；handleComment/handleSimpleTag(BR)/handleEndOfLineString 可调用。
4. `kit.createDefaultDocument().getReader(0)` instanceof HTMLReader。
