# javax.swing.text.AbstractDocument.LeafElement — DESIGN

## 1. API 功能
`AbstractElement` 的叶子子类，代表无子元素的文本片段（如一行）。方法分组：
- 元素：`getStartOffset()`、`getEndOffset()`、`getName()`、`getElementIndex(int)`、`getElement(int)`、`getElementCount()`、`isLeaf()`。
- TreeNode：`getAllowsChildren()`、`children()`。
- 调试：`toString()`。

## 2. Java 用户如何使用
非 static 内部类，可 `doc.new LeafElement(parent, attrs, offs0, offs1)` 直接构造；更常见的是经文档元素树获得：`PlainDocument doc; doc.insertString(0,"a\nb",null); Element line = doc.getParagraphElement(0);` 即 `LeafElement`，再读取 `getStartOffset/getEndOffset`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/swing/text/PlainDocument.java:152`：`Element line = createLeafElement(map, null, 0, 1);`。
- `src/java.desktop/share/classes/javax/swing/text/AbstractDocument.java:2529`：`public class LeafElement extends AbstractElement`，偏移通过 `p0.getOffset()/p1.getOffset()` 实现。
- `test/jdk/javax/swing/text/WrappedPlainView/6857057/StubLeafElement.java`：测试桩子类化 `LeafElement`。

## 4. 测试用例设计 + 自动化实现（`LeafElementTest.testBlackBox`）
- 直接构造 `LeafElement(null,null,3,8)`，验证 `isLeaf/getStartOffset/getEndOffset/getName/getElementCount==0/getElement==null/getElementIndex==-1/getAllowsChildren==false/children()==null/toString`。
- 经 `PlainDocument` 插入 "hello\nworld"，验证两行 `LeafElement` 的区间、`getParagraphElement` 映射、父元素与所属文档。
