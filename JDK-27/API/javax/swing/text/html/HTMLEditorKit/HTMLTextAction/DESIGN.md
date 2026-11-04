# javax.swing.text.html.HTMLEditorKit.HTMLTextAction — DESIGN

## 1. API 功能
HTML 文本编辑动作的抽象基类（`extends StyledEditorKit.StyledTextAction`），构造器 `HTMLTextAction(String name)`。提供 protected 便捷方法：
- `getHTMLDocument(JEditorPane)`：取编辑器的 HTMLDocument，否则抛 `IllegalArgumentException`。
- `getHTMLEditorKit(JEditorPane)`：取编辑器的 HTMLEditorKit，否则抛 IAE。
- `getElementsAt(HTMLDocument,int)`：返回从根到叶包含 offset 的元素链（首元素为根）。
- `elementCountToTag(HTMLDocument,int,HTML.Tag)`：从叶向上数到指定 tag 的元素个数，找不到返回 -1。
- `findElementMatchingTag(HTMLDocument,int,HTML.Tag)`：返回 offset 处最深层匹配 tag 的元素。

## 2. Java 用户如何使用
子类化并 `super(name)`，在 `actionPerformed` 里用 `getHTMLDocument(getEditor(e))`、`getHTMLEditorKit(...)` 拿到文档/工具包，再用 `getElementsAt`/`elementCountToTag`/`findElementMatchingTag` 定位插入点。JDK 内置 `InsertHTMLTextAction`、`NavigateLinkAction`、`ActivateLinkAction` 都继承它。

## 3. 开源代码实际用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLEditorKit.java`：`InsertHTMLTextAction extends HTMLTextAction`，其 `actionPerformed` 内 `HTMLDocument doc = getHTMLDocument(editor); ... insertIntoTag(...)`；`getHTMLDocument` 实现 `if (d instanceof HTMLDocument) return (HTMLDocument) d; throw new IllegalArgumentException("document must be HTMLDocument");`。
- 同文件 `NavigateLinkAction`/`ActivateLinkAction`：`ElementIterator ei = new ElementIterator(doc);` 依赖 HTMLDocument 结构，属 HTMLTextAction 家族。
- 同文件 `defaultActions`：`new InsertHTMLTextAction("InsertTable", ..., HTML.Tag.BODY, HTML.Tag.TABLE)` 等 9 个 HTMLTextAction 实例由 `getActions()` 返回。

## 4. 测试用例设计（黑盒部分，`HTMLTextActionTest.testBlackBox`）
1. `kit.getActions()` 中统计 `instanceof HTMLTextAction` 数量 > 0。
2. 具体子类 probe：`getValue(Action.NAME)` 往返构造名。
3. 在 HTMLEditorKit + HTMLDocument 的 JEditorPane 上 `getHTMLDocument`/`getHTMLEditorKit` 返回同一实例。
4. `getElementsAt(doc,1)` 长度 ≥2 且 `[0].getName()=="html"`；`elementCountToTag(BODY)>=0`、`elementCountToTag(TABLE)==-1`；`findElementMatchingTag(BODY)` 名 "body"。
5. 对 `StyledEditorKit` 编辑器，`getHTMLDocument`/`getHTMLEditorKit` 抛精确 `IllegalArgumentException`。
