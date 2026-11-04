# javax.swing.text.html.HTMLDocument.HTMLReader.CharacterAction — DESIGN

## 1. API 功能
HTML 字符级标签的解析动作（`HTMLReader` 的嵌套 public 类，`extends HTMLDocument.HTMLReader.TagAction`，非抽象，无接口）。核心方法分组：
- 覆写 `start(HTML.Tag, MutableAttributeSet)`、`end(HTML.Tag)`：处理字符级标签（如 B、BIG、CODE、EM）的开始/结束，维护字符样式与 run 结构。

## 2. Java 用户如何使用
通过 `doc.new HTMLReader(0).new CharacterAction()` 获得实例，由 HTMLReader 的 tagMap 在解析到字符级标签时回调：`action.start(HTML.Tag.B, attrs)` → ... → `action.end(HTML.Tag.B)`。典型链：start(B) → end(B)。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLDocument.java:2438`：`TagAction ca = new CharacterAction();` 构造后注册到 tagMap。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLDocument.java:2452`：`tagMap.put(HTML.Tag.ADDRESS, ca);` 将多个字符级标签绑定到 CharacterAction。

## 4. 测试用例设计（黑盒部分，`CharacterActionTest.testBlackBox`）
1. `reader.new CharacterAction()` instanceof TagAction。
2. `action.start(HTML.Tag.B, attrs)`、`action.end(HTML.Tag.B)` 正常完成无异常。
