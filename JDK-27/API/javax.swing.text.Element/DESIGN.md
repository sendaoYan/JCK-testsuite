# javax.swing.text.Element — DESIGN

## 1. API 功能
文档结构元素的接口（树形层级中的节点）。核心方法：
- 关联：`getDocument()`、`getParentElement()`、`getName()`、`getAttributes()`。
- 区间：`getStartOffset()`、`getEndOffset()`。
- 子元素：`getElement(int)`、`getElementCount()`、`getElementIndex(int)`。
- 形态：`isLeaf()`。

## 2. Java 用户如何使用
典型组合链路：`doc.getDefaultRootElement()` 得到根 → `getElementCount()`/`getElement(i)` 遍历子元素 → `getStartOffset()/getEndOffset()` 取区间 → `getElementIndex(pos)` 定位。例如：
```java
Element root = doc.getDefaultRootElement();
Element line = root.getElement(root.getElementIndex(pos));
int start = line.getStartOffset(), end = line.getEndOffset();
```

## 3. 开源代码实际用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/PlainDocument.java`：`getDefaultRootElement()` 返回 `defaultRoot`，`createDefaultRoot()` 构造一个 branch 根 + 一个 leaf 行；`getParagraphElement(pos)` 用 `getElement(getElementIndex(pos))` 定位。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/AbstractDocument.java`：`BranchElement.getName()` 无属性名时回退为 `ParagraphElementName`（"paragraph"），`LeafElement.getName()` 回退为 `ContentElementName`（"content"）。

## 4. 测试用例设计 + 自动化实现（`ElementTest.testBlackBox`）
基于 `PlainDocument`（headless）验证 Element 契约：
1. 根元素：getDocument 恒等、无 parent、非 leaf、名 "paragraph"、空属性、初始 1 个子元素。
2. 叶子元素：parent 是根、isLeaf、名 "content"、区间 [0,12)（含隐含换行）、`getElementIndex` 正确映射。
3. 组合操作：`insertString("\nsecond")` 后结构变为 2 个叶子，第二个叶子区间 [12,19)，`getElementIndex(12)==1`。
