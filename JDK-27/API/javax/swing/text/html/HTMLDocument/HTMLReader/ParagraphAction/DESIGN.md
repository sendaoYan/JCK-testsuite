# javax.swing.text.html.HTMLDocument.HTMLReader.ParagraphAction — DESIGN

## 1. API 功能
HTML 段落级标签的解析动作（`HTMLReader` 的嵌套 public 类，`extends HTMLDocument.HTMLReader.BlockAction`，非抽象，无接口）。核心方法分组：
- 覆写 `start(HTML.Tag, MutableAttributeSet)`、`end(HTML.Tag)`：处理段落级标签（如 P、H1-H6、DT）的开始/结束，维护段落结构。

## 2. Java 用户如何使用
通过 `doc.new HTMLReader(0).new ParagraphAction()` 获得实例，由 HTMLReader 的 tagMap 在解析到段落标签时回调：`action.start(HTML.Tag.P, attrs)` → ... → `action.end(HTML.Tag.P)`。典型链：start(P) → end(P)。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLDocument.java:2437`：`TagAction pa = new ParagraphAction();` 构造后注册到 tagMap。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLDocument.java:2496`：`tagMap.put(HTML.Tag.P, pa);` 将 P、H1-H6、DT 等绑定到 ParagraphAction。

## 4. 测试用例设计（黑盒部分，`ParagraphActionTest.testBlackBox`）
1. `reader.new ParagraphAction()` instanceof BlockAction。
2. `action.start(HTML.Tag.P, attrs)`、`action.end(HTML.Tag.P)` 正常完成无异常。
