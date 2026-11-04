# javax.swing.text.html.HTMLDocument.HTMLReader.HiddenAction — DESIGN

## 1. API 功能
HTML 隐藏标签的解析动作（`HTMLReader` 的嵌套 public 类，`extends HTMLDocument.HTMLReader.TagAction`，非抽象，无接口）。核心方法分组：
- 覆写 `start(HTML.Tag, MutableAttributeSet)`、`end(HTML.Tag)`：处理不应渲染内容的标签（如 META、SCRIPT、APPLET）。

## 2. Java 用户如何使用
通过 `doc.new HTMLReader(0).new HiddenAction()` 获得实例，由 HTMLReader 的 tagMap 在解析到隐藏标签时回调：`action.start(HTML.Tag.META, attrs)`。典型链：start(META)。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLDocument.java:2441`：`TagAction ha = new HiddenAction();` 构造后注册到 tagMap。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLDocument.java:2447`：`tagMap.put(HTML.Tag.APPLET, ha);` 将 APPLET/SCRIPT 等绑定到 HiddenAction。

## 4. 测试用例设计（黑盒部分，`HiddenActionTest.testBlackBox`）
1. `reader.new HiddenAction()` instanceof TagAction。
2. `action.start(HTML.Tag.META, attrs)` 正常完成无异常。
