# javax.swing.text.html.FormSubmitEvent — DESIGN

## 1. API 功能
表单提交超链接事件（`extends HTMLFrameHyperlinkEvent`；JDK-8 非 final、JDK-9+ final）。核心方法分组：
- 查询：`getMethod()`（返回 `FormSubmitEvent.MethodType`，GET/POST）、`getData()`（表单提交数据字符串）。
- 继承：`getURL()`、`getEventType()`、`getTarget()`、`getSource()`、`getSourceElement()`、`getDescription()`。
- 构造器包私有，由 FormView 在提交时派发。

## 2. Java 用户如何使用
用户在 `HyperlinkListener.hyperlinkUpdate` 中 `instanceof FormSubmitEvent` 判断后，`getMethod()` 判断 GET/POST、`getData()` 拿表单数据。典型链：`evt.getMethod() == FormSubmitEvent.MethodType.POST && evt.getData().contains("q=")`。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/html/FormView.java`：`FormSubmitEvent.MethodType.POST/GET` 二选一后 `new FormSubmitEvent(...)` 派发。
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/html/HTMLEditorKit.java`：`setAutoFormSubmission` 控制自动提交还是仅派发事件。

## 4. 测试用例设计（黑盒，`FormSubmitEventTest`：testShape/testMethodType/testReflectiveConstruction，36 断言）
1. testShape：superclass HTMLFrameHyperlinkEvent、final 修饰版本分支（JDK-8 not final / JDK-9+ final）、无公有构造器、getMethod/getData 方法存在。
2. testMethodType：MethodType 恰 2 个常量、声明顺序 GET 后 POST、ordinal 0/1、name()/toString()、compareTo GET<POST、valueOf 双向、valueOf(unknown) IAE、valueOf(null) NPE（精确异常）。
3. testReflectiveConstruction：包私有构造器 setAccessible 后构造 GET/POST 事件，getMethod/getData/getEventType/getURL/getTarget/getSource/getSourceElement 往返（JDK-9+ 需 `--add-opens=java.desktop/javax.swing.text.html=ALL-UNNAMED`）。
