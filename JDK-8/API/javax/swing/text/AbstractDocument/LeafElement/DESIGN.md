# javax.swing.text.AbstractDocument.LeafElement — DESIGN

## 1. API 功能
`AbstractElement` 的叶子子类，代表无子元素的文本片段（如一行）。方法分组：
- 元素：`getStartOffset()`、`getEndOffset()`、`getName()`、`getElementIndex(int)`、`getElement(int)`、`getElementCount()`、`isLeaf()`。
- TreeNode：`getAllowsChildren()`、`children()`。
- 调试：`toString()`。

## 2. Java 用户如何使用
非 static 内部类，可 `doc.new LeafElement(parent, attrs, offs0, offs1)` 直接构造；更常见的是经文档元素树获得：`PlainDocument doc; doc.insertString(0,"a\nb",null); Element line = doc.getParagraphElement(0);` 即 `LeafElement`，再读取 `getStartOffset/getEndOffset`。

## 3. 开源代码实际用法调研（来源：jdk8u 源码 + jdk/test）
- `jdk/src/share/classes/javax/swing/text/PlainDocument.java:152`：`Element line = createLeafElement(map, null, 0, 1);`。
- `jdk/src/share/classes/javax/swing/text/AbstractDocument.java:2529`：`public class LeafElement extends AbstractElement`，偏移通过 `p0.getOffset()/p1.getOffset()` 实现。
- `jdk/test/javax/swing/text/WrappedPlainView/6857057/StubLeafElement.java`：测试桩子类化 `LeafElement`。

## 4. 测试用例设计 + 自动化实现
`testBehavior`（31 断言）：
- PlainDocument 行元素就是 LeafElement（名 "content"）；区间精确 [2,5)；isLeaf true、getAllowsChildren false、children() 返回 null（不是空枚举）、getElement 恒 null、getElementCount 0、getElementIndex 返回 -1。
- toString 精确格式 "LeafElement(content) 2,5\n"（换行结尾）。
- 属性：未持锁修改 → StateInvariantError；锁内 addAttribute 生效；ElementNameAttribute 覆盖名字；null 属性集容忍。
