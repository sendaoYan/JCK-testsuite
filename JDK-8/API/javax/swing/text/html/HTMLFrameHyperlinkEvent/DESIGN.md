# javax.swing.text.html.HTMLFrameHyperlinkEvent — DESIGN

## 1. API 功能
带 target frame 的 HTML 超链接事件（`extends HyperlinkEvent`，非抽象，无接口）。核心方法分组：
- 构造器（5 个公有）：4/5/6/7 参（source, EventType, URL, target）/（+description）/（+Element）/（+description+Element）/（+InputEvent+target 组合）。
- 查询：`getTarget()`（目标 frame 名）。
- 继承：`getURL()`、`getEventType()`、`getSource()`、`getDescription()`、`getSourceElement()`、`getInputEvent()`。

## 2. Java 用户如何使用
在 HyperlinkListener 中收到或自行构造：`new HTMLFrameHyperlinkEvent(src, ACTIVATED, url, "main")` 后 `getTarget()` 拿目标 frame，交给 `HTMLDocument.processHTMLFrameHyperlinkEvent` 处理。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/html/HTMLEditorKit.java`：`linkEvent = new HTMLFrameHyperlinkEvent(html, ACTIVATED, u, href, element, mouseEvent, target);`。
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/JEditorPane.java`：`if (e instanceof HTMLFrameHyperlinkEvent) { ... doc.processHTMLFrameHyperlinkEvent(evt); }`。

## 4. 测试用例设计（黑盒，`HTMLFrameHyperlinkEventTest`：testShape/testConstructors，35 断言）
1. testShape：superclass HyperlinkEvent、5 个公有构造器、getTarget 方法存在。
2. testConstructors：逐构造器断言 getTarget/getURL/getEventType/getSource/getDescription/getSourceElement/getInputEvent 的精确值与 null 语义（ctor1 description null、source element null；Element 构造 getSourceElement 身份；7 参构造 InputEvent null 等）。
