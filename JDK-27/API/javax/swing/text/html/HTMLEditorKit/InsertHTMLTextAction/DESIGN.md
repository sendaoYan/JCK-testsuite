# javax.swing.text.html.HTMLEditorKit.InsertHTMLTextAction — DESIGN

## 1. API 功能
向现有 HTML 文档插入任意 HTML 串的动作（`extends HTMLTextAction`）。构造器：
- `InsertHTMLTextAction(String name, String html, HTML.Tag parentTag, HTML.Tag addTag)`；
- 六参版本加 `alternateParentTag`/`alternateAddTag`（无 parentTag 时回退尝试）。
核心 `actionPerformed(ActionEvent)`：从事件源取 `JEditorPane` → `getHTMLDocument` → `insertIntoTag`/`insertAtBoundary` 定位插入点 → 调 `HTMLEditorKit.insertHTML` 落库，并可选 `adjustSelection`。

## 2. Java 用户如何使用
`new InsertHTMLTextAction("插入加粗", "<b>hello</b>", HTML.Tag.BODY, HTML.Tag.B)` 得到动作 → `action.actionPerformed(new ActionEvent(jep, ActionEvent.ACTION_PERFORMED, "cmd"))`；或从 `HTMLEditorKit.getActions()` 取内置动作（InsertTable/InsertUnorderedList/InsertOrderedList/InsertPre/InsertHR 等）。插入后 `doc.getText(0, doc.getLength())` 即含新文本。

## 3. 开源代码实际用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLEditorKit.java`：`defaultActions` 里 `new InsertHTMLTextAction("InsertTable", INSERT_TABLE_HTML, HTML.Tag.BODY, HTML.Tag.TABLE)`、`new InsertHTMLTextAction("InsertUnorderedListItem", INSERT_UL_HTML, HTML.Tag.UL, HTML.Tag.LI, HTML.Tag.BODY, HTML.Tag.UL)` 等，构成标准编辑菜单动作。
- 同文件 `actionPerformed`：`JEditorPane editor = getEditor(ae); ... HTMLDocument doc = getHTMLDocument(editor); int offset = editor.getSelectionStart();` 再 `insertIntoTag(...)` 并 `adjustSelection(...)`。

## 4. 测试用例设计（黑盒部分，`InsertHTMLTextActionTest.testBlackBox`）
1. 4 参/6 参构造器 `getValue(Action.NAME)` 往返，且 `instanceof HTMLTextAction`。
2. 在 `new JEditorPane()` 设 HTMLEditorKit + HTMLDocument 后 `actionPerformed`，`doc.getText` 含 "hello" 且文档非空。
3. 第二次 actionPerformed 继续追加内容（组合操作）。
4. 事件源非 `JTextComponent` 时 actionPerformed 无异常（no-op）。
