# javax.swing.text.html.HTMLDocument.HTMLReader.PreAction — DESIGN

## 1. API 功能
HTML `<pre>` 标签的解析动作（`HTMLReader` 的嵌套 public 类，`extends HTMLDocument.HTMLReader.BlockAction`，非抽象，无接口）。核心方法分组：
- 覆写 `start(HTML.Tag, MutableAttributeSet)`、`end(HTML.Tag)`：处理 PRE（预格式化）标签的开始/结束，保留空白与换行。

## 2. Java 用户如何使用
通过 `doc.new HTMLReader(0).new PreAction()` 获得实例，由 HTMLReader 的 tagMap 在解析到 PRE 时回调：`action.start(HTML.Tag.PRE, attrs)` → ... → `action.end(HTML.Tag.PRE)`。典型链：start(PRE) → end(PRE)。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLDocument.java:2497`：`tagMap.put(HTML.Tag.PRE, new PreAction());` 将 PRE 绑定到 PreAction。

## 4. 测试用例设计（黑盒部分，`PreActionTest.testBlackBox`）
1. `reader.new PreAction()` instanceof BlockAction。
2. `action.start(HTML.Tag.PRE, attrs)`、`action.end(HTML.Tag.PRE)` 正常完成无异常。
