# javax.swing.text.html.HTMLEditorKit.LinkController — DESIGN

## 1. API 功能
超链接鼠标事件处理器（`extends MouseAdapter`，implements `MouseMotionListener`/`Serializable`），公开无参构造器。`mouseClicked/mouseMoved/mouseDragged` 定位光标下的 `A` 元素并触发 HyperlinkEvent；protected `activateLink(int, JEditorPane)` 对指定位置触发链接激活（内部 `createHyperlinkEvent` + `fireHyperlinkUpdate`），`getMapHREF` 处理 `MAP` 区域链接。

## 2. Java 用户如何使用
一般不直接实例化；`HTMLEditorKit` 内部持有 `linkHandler = new LinkController()` 并挂到编辑器监听鼠标。测试时通过子类暴露 protected `activateLink(pos, editor)`，配合 `JEditorPane.addHyperlinkListener` 观察 `HyperlinkEvent.EventType.ACTIVATED` 事件及其 `description`（href）。

## 3. 开源代码实际用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLEditorKit.java`：字段 `private LinkController linkHandler = new LinkController();`；`activateLink(int pos, JEditorPane html, MouseEvent mouseEvent)` 内 `Element e = hdoc.getCharacterElement(pos); AttributeSet anchor = (AttributeSet)a.getAttribute(HTML.Tag.A); ... html.fireHyperlinkUpdate(linkEvent);`。
- 同文件 `createHyperlinkEvent`：`linkEvent = new HyperlinkEvent(html, HyperlinkEvent.EventType.ACTIVATED, u, href, element, mouseEvent);`。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/html/HTMLEditorKit/bug4102068.java`：验证超链接悬停时 HAND 光标，即 LinkController 的 mouseMoved 行为。

## 4. 测试用例设计（黑盒部分，`LinkControllerTest.testBlackBox`）
1. `new LinkController()` 可实例化。
2. 构建含 `<a href="http://example.com/x">link</a>` 的文档，子类暴露 `activateLink(1, pane)` 后：恰好触发 1 个 HyperlinkEvent、类型 ACTIVATED、`description` 为 href、`source` 为编辑器。
