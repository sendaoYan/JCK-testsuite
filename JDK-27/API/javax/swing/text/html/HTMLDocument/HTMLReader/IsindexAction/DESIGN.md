# javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction — DESIGN

## 1. API 功能
HTML `<isindex>` 标签的解析动作（`HTMLReader` 的嵌套 public 类，`extends HTMLDocument.HTMLReader.TagAction`，非抽象，无接口）。核心方法分组：
- 覆写 `start(HTML.Tag, MutableAttributeSet)`、`end(HTML.Tag)`：处理 ISINDEX 标签（历史搜索输入控件）。

## 2. Java 用户如何使用
通过 `doc.new HTMLReader(0).new IsindexAction()` 获得实例，由 HTMLReader 的 tagMap 在解析到 ISINDEX 时回调：`action.start(HTML.Tag.ISINDEX, attrs)`。典型链：start(ISINDEX)。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLDocument.java:2483`：`tagMap.put(HTML.Tag.ISINDEX, new IsindexAction());` 将 ISINDEX 绑定到 IsindexAction。

## 4. 测试用例设计（黑盒部分，`IsindexActionTest.testBlackBox`）
1. `reader.new IsindexAction()` instanceof TagAction。
2. `action.start(HTML.Tag.ISINDEX, attrs)` 正常完成无异常。
