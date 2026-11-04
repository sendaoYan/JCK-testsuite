# javax.swing.text.html.HTMLDocument.BlockElement — DESIGN

## 1. API 功能
HTML 块级元素（`HTMLDocument` 的嵌套 public 类，`extends AbstractDocument.BranchElement`，非抽象，无接口）。核心方法分组：
- 结构查询（继承 BranchElement/Element）：`getName()`、`getElement(int)`、`getParentElement()`、`getStartOffset()`、`getEndOffset()`。
- 由 `HTMLDocument.createBranchElement` 创建，代表 html/head/body/p 等块级节点。

## 2. Java 用户如何使用
解析 HTML 后通过 `doc.getDefaultRootElement()`、`root.getElement(0)` 或 `doc.getParagraphElement(off)` 获得 BlockElement，用 `getName()` 判断标签、`getParentElement()` 找父、`getStartOffset()/getEndOffset()` 取范围。典型链：`root.getElement(1).getName()`。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLDocument.java:609`：`createBranchElement` 返回 `new BlockElement(parent, a)`，是文档分支元素的唯一来源。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLDocument.java:626-633`：默认结构里 `new BlockElement(null, ...)` 构造 html，再嵌套 body、paragraph 等。

## 4. 测试用例设计（黑盒部分，`BlockElementTest.testBlackBox`）
1. root（getDefaultRootElement）instanceof BlockElement、getName()=="html"。
2. root.getElement(0)/getElement(1) 均 instanceof BlockElement，名分别为 "head"/"body"。
3. getParagraphElement(1) instanceof BlockElement、getName()=="p"。
4. paragraph 的 getParentElement()==body。
5. paragraph 的 getStartOffset()<=getEndOffset()（偏移有序）。
