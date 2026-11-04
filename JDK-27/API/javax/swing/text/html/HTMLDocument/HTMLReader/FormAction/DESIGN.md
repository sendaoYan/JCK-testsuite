# javax.swing.text.html.HTMLDocument.HTMLReader.FormAction — DESIGN

## 1. API 功能
HTML 表单控件的解析动作（`HTMLReader` 的嵌套 public 类，`extends HTMLDocument.HTMLReader.SpecialAction`，非抽象，无接口）。核心方法分组：
- 覆写 `start(HTML.Tag, MutableAttributeSet)`、`end(HTML.Tag)`：处理表单控件标签（如 INPUT、OPTION、SELECT）的开始/结束，构建表单模型。

## 2. Java 用户如何使用
通过 `doc.new HTMLReader(0).new FormAction()` 获得实例，由 HTMLReader 的 tagMap 在解析到表单控件时回调：`action.start(HTML.Tag.FORM, attrs)` → ... → `action.end(HTML.Tag.FORM)`。典型链：start → end。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLDocument.java:2440`：`TagAction fa = new FormAction();` 构造后注册到 tagMap。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLDocument.java:2484`：`tagMap.put(HTML.Tag.INPUT, fa);` 将 INPUT/OPTION/SELECT 绑定到 FormAction。

## 4. 测试用例设计（黑盒部分，`FormActionTest.testBlackBox`）
1. `reader.new FormAction()` instanceof SpecialAction。
2. `action.start(HTML.Tag.FORM, attrs)`、`action.end(HTML.Tag.FORM)` 正常完成无异常。
