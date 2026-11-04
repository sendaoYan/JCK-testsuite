# javax.swing.text.html.HTMLFrameHyperlinkEvent — DESIGN

## 1. API 功能
带 target frame 的 HTML 超链接事件（`extends HyperlinkEvent`，非抽象，无接口）。核心方法分组：
- 构造器（4/5/6/7 参）：`(source, EventType, URL, target)`、`(source, EventType, URL, description, target)`、`(source, EventType, URL, Element, target)`、`(source, EventType, URL, description, Element, target)`、`(source, EventType, URL, description, Element, InputEvent, target)`。
- 查询：`getTarget()`（目标 frame 名）。
- 继承：`getURL()`、`getEventType()`、`getSource()`、`getDescription()`、`getSourceElement()`、`getInputEvent()`。

## 2. Java 用户如何使用
用户（通常在 HyperlinkListener 中）构造或收到该事件后 `getTarget()` 拿目标 frame、`getURL()` 拿地址，交给 `HTMLDocument.processHTMLFrameHyperlinkEvent` 处理。典型链：`new HTMLFrameHyperlinkEvent(src, ACTIVATED, url, "main").getTarget()`。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLEditorKit.java:995`：`linkEvent = new HTMLFrameHyperlinkEvent(html, ACTIVATED, u, href, element, mouseEvent, target);` 构造带 target 的链接事件。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JEditorPane.java:167-170`：`if (e instanceof HTMLFrameHyperlinkEvent) { ... doc.processHTMLFrameHyperlinkEvent(evt); }` 判断并处理。

## 4. 测试用例设计（黑盒部分，`HTMLFrameHyperlinkEventTest.testBlackBox`）
1. 4 参构造：getTarget()=="main"、getURL()、getEventType()==ACTIVATED、getSource()。
2. 5 参构造：getTarget()=="left"、getDescription()=="desc"。
3. Element 构造：getTarget()=="right"、getSourceElement()==elem。
4. 6 参构造：getTarget()=="top"、getDescription()=="d"、getSourceElement()==elem。
5. 7 参构造：getTarget()=="self"、getInputEvent()==null。
