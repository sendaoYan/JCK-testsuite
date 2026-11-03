# javax.swing.text.html.HTMLDocument.RunElement — DESIGN

## 1. API 功能
HTML 文档叶子元素（`HTMLDocument` 的嵌套 public 类，`extends AbstractDocument.LeafElement`，非抽象，无接口）。核心方法分组：
- 查询（继承 LeafElement/Element）：`getName()`（返回 "content"）、`isLeaf()`、`getParentElement()`、`getStartOffset()`、`getEndOffset()`。
- 由 `HTMLDocument.createLeafElement` 创建，代表段落内的字符级 run。

## 2. Java 用户如何使用
解析 HTML 后通过 `doc.getCharacterElement(offset)` 获得 RunElement，用 `getName()` 判断（"content"）、`isLeaf()` 确认叶子、`getParentElement()` 找所属段落。典型链：`doc.getCharacterElement(para.getStartOffset()).getName()`。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLDocument.java:596`：`createLeafElement` 返回 `new RunElement(parent, a, p0, p1)`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLDocument.java:587`：类 javadoc 说明叶子元素类型为 `HTMLDocument.RunElement`。

## 4. 测试用例设计（黑盒部分，`RunElementTest.testBlackBox`）
1. `doc.getCharacterElement(para.getStartOffset())` instanceof RunElement、getName()=="content"。
2. 段落内每个偏移的字符级元素都是 RunElement。
3. isLeaf()==true、getStartOffset()<=getEndOffset()、getParentElement() 非 null。
