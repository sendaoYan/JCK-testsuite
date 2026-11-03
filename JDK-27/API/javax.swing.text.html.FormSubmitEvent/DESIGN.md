# javax.swing.text.html.FormSubmitEvent — DESIGN

## 1. API 功能
表单提交超链接事件（final，`extends HTMLFrameHyperlinkEvent`，无接口）。核心方法分组：
- 查询：`getMethod()`（返回 `FormSubmitEvent.MethodType`，GET/POST）、`getData()`（表单提交数据字符串）。
- 继承自 HyperlinkEvent/HTMLFrameHyperlinkEvent：`getURL()`、`getEventType()`、`getTarget()`、`getSource()`、`getSourceElement()`、`getDescription()`。
- 构造器为包私有，用户不直接 new，通常由 HTMLEditorKit 在表单提交时派发。

## 2. Java 用户如何使用
用户在 `HyperlinkListener.hyperlinkUpdate` 中收到 `FormSubmitEvent`：`instanceof FormSubmitEvent` 判断后，`getMethod()` 判断 GET/POST、`getData()` 拿表单数据、`getURL()` 拿 action 地址、`getTarget()` 拿目标 frame。典型链：`evt.getMethod() == FormSubmitEvent.MethodType.POST && evt.getData().contains("q=")`。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/FormView.java:536-545`：`FormSubmitEvent.MethodType.POST/GET` 二选一后 `new FormSubmitEvent(FormView.this, EventType.ACTIVATED, actionURL, form, target, methodType, data)` 派发提交事件。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/FrameView.java:312`：`if (evt instanceof FormSubmitEvent)` 判断事件类型。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLEditorKit.java:606`：setAutoFormSubmission 控制自动提交还是仅派发 `FormSubmitEvent`。

## 4. 测试用例设计（黑盒部分，`FormSubmitEventTest.testBlackBox`）
1. POST 事件：getMethod()==POST、getData()=="a=1&b=2"、getEventType()==ACTIVATED、getURL()、getTarget()、getSource()、getSourceElement().getName()=="html"。
2. GET 事件：getMethod()==GET、getData()=="q=java"。
