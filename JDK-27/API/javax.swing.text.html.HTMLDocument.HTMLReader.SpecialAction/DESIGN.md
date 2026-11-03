# javax.swing.text.html.HTMLDocument.HTMLReader.SpecialAction — DESIGN

## 1. API 功能
HTML 特殊（单标签）标签的解析动作（`HTMLReader` 的嵌套 public 类，`extends HTMLDocument.HTMLReader.TagAction`，非抽象，无接口）。核心方法分组：
- 覆写 `start(HTML.Tag, MutableAttributeSet)`、`end(HTML.Tag)`：处理自包含/特殊标签（如 IMG、BR、HR、OBJECT）的开始/结束。

## 2. Java 用户如何使用
通过 `doc.new HTMLReader(0).new SpecialAction()` 获得实例，由 HTMLReader 的 tagMap 在解析到特殊标签时回调：`action.start(HTML.Tag.IMG, attrs)`。典型链：start(IMG)。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLDocument.java:2439`：`TagAction sa = new SpecialAction();` 构造后注册到 tagMap。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLDocument.java:2477`：`tagMap.put(HTML.Tag.IMG, sa);` 将 IMG/BR/HR/OBJECT 等绑定到 SpecialAction。

## 4. 测试用例设计（黑盒部分，`SpecialActionTest.testBlackBox`）
1. `reader.new SpecialAction()` instanceof TagAction。
2. `action.start(HTML.Tag.IMG, attrs)` 正常完成无异常。
