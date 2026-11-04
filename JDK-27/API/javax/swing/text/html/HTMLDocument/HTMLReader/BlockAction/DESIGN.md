# javax.swing.text.html.HTMLDocument.HTMLReader.BlockAction — DESIGN

## 1. API 功能
HTML 块级标签的解析动作（`HTMLReader` 的嵌套 public 类，`extends HTMLDocument.HTMLReader.TagAction`，非抽象，无接口）。核心方法分组：
- 覆写 `start(HTML.Tag, MutableAttributeSet)`、`end(HTML.Tag)`：处理块级标签（如 BODY、BLOCKQUOTE、DIV）的开始/结束，维护文档块结构。

## 2. Java 用户如何使用
通过 `doc.new HTMLReader(0).new BlockAction()` 获得实例，由 HTMLReader 的 tagMap 在解析到块级标签时回调：`action.start(HTML.Tag.BODY, attrs)` → ... → `action.end(HTML.Tag.BODY)`。典型链：start(BODY) → end(BODY)。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLDocument.java:2436`：`TagAction ba = new BlockAction();` 构造后注册到 tagMap。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLDocument.java:2449`：`tagMap.put(HTML.Tag.BODY, ba);` 将 BODY 等块级标签绑定到 BlockAction。

## 4. 测试用例设计（黑盒部分，`BlockActionTest.testBlackBox`）
1. `reader.new BlockAction()` instanceof TagAction。
2. `action.start(HTML.Tag.BODY, attrs)`、`action.end(HTML.Tag.BODY)` 正常完成无异常。
