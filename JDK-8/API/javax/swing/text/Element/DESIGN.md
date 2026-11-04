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
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/PlainDocument.java`：`getDefaultRootElement()` 返回 `defaultRoot`，`createDefaultRoot()` 构造一个 branch 根 + 一个 leaf 行；`getParagraphElement(pos)` 用 `getElement(getElementIndex(pos))` 定位。
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/AbstractDocument.java`：`BranchElement.getName()` 无属性名时回退为 `ParagraphElementName`（"paragraph"），`LeafElement.getName()` 回退为 `ContentElementName`（"content"）。

## 4. 测试用例设计 + 自动化实现
`testPlainTree/testStyledTree/testInvalidIndices`（60 断言）：
- 接口形状：11 方法逐一存在；非枚举/注解。
- PlainDocument 树：root getDocument identity、parent null、名 "paragraph"、区间 [0,6)、两行元素 [0,3)/[3,6)、children parent 回链、行为 leaf、行名 "content"、行无子。
- 结构变更：插入 "\n" 精确拆分（中段 [2,4)）；删除换行合并。
- DefaultStyledDocument 树：root 名 "section"、两段、段名 "paragraph" 且为 branch、每段一个 "content" run。
- 反例矩阵：getElement(-1) → ArrayIndexOutOfBoundsException；getElement(count) → null（不抛）。
