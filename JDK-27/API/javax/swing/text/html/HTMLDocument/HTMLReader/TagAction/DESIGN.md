# javax.swing.text.html.HTMLDocument.HTMLReader.TagAction — DESIGN

## 1. API 功能
HTML 标签解析动作基类（`HTMLReader` 的嵌套 public 类，`extends Object`，非抽象，无接口）。核心方法分组：
- `start(HTML.Tag, MutableAttributeSet)`、`end(HTML.Tag)`：默认空实现（no-op），供子类覆写。
- 是 BlockAction/CharacterAction/SpecialAction/FormAction/HiddenAction/IsindexAction 等的共同父类。

## 2. Java 用户如何使用
通过 `doc.new HTMLReader(0).new TagAction()` 获得基类实例；子类覆写 start/end 以在标签开始/结束做文档编辑。基类实例的 `start(HTML.Tag.A, attrs)`、`end(HTML.Tag.A)` 为文档化 no-op。典型链：start(A) → end(A)。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLDocument.java:2435`：`TagAction na = new TagAction();` 作为默认动作。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLDocument.java:2436-2441`：BlockAction/ParagraphAction/CharacterAction/SpecialAction/FormAction/HiddenAction 均声明为 TagAction 类型并派发给标签。

## 4. 测试用例设计（黑盒部分，`TagActionTest.testBlackBox`）
1. `reader.new TagAction()` 非 null 且 instanceof TagAction。
2. 默认 `start(HTML.Tag.A, attrs)`、`end(HTML.Tag.A)` 无异常完成（no-op）。
