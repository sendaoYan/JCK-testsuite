# javax.swing.text.AbstractDocument.BranchElement — DESIGN

## 1. API 功能
`AbstractElement` 的复合子类，代表包含子元素的节点。方法分组：
- 结构：`positionToElement(int)`、`replace(int,int,Element[])`。
- 元素：`getName()`、`getStartOffset()`、`getEndOffset()`、`getElement(int)`、`getElementCount()`、`getElementIndex(int)`、`isLeaf()`。
- TreeNode：`getAllowsChildren()`、`children()`。
- 调试：`toString()`。

## 2. Java 用户如何使用
`BranchElement` 是非 static 内部类，需经文档实例创建：`doc.new BranchElement(parent, attrs)`；随后 `replace(0,0,new Element[]{...})` 填充子元素，再用 `positionToElement/getElementIndex/getStartOffset/getEndOffset` 查询。文档默认根（`PlainDocument.getDefaultRootElement()`）本身就是 `BranchElement`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/swing/text/PlainDocument.java:150`：`BranchElement map = (BranchElement) createBranchElement(null, null); ... map.replace(0, 0, lines);`。
- `src/java.desktop/share/classes/javax/swing/text/AbstractDocument.java:2314`：`BranchElement.replace` 是行映射重排的核心。
- `test/jdk/javax/swing/text/WrappedPlainView/6857057/StubBranchElement.java`：测试桩直接继承 `BranchElement` 并覆盖 `getName` 等。

## 4. 测试用例设计 + 自动化实现（`BranchElementTest.testBlackBox`）
- 直接 `doc.new BranchElement(null,null)`：空子元素下的 `isLeaf/getElementCount/getName/getAllowsChildren/children()==null`。
- `replace` 加入两个 `LeafElement` 后验证 `getStartOffset/getEndOffset/getElement/getElementIndex/positionToElement`，越界 `positionToElement` 返回 null，`children()` 枚举 2 项，`toString` 前缀。
- 经 `PlainDocument` 验证默认根是 `BranchElement`、行数、首行区间、`getParagraphElement` 映射。

## 5. 契约深化（本轮追加）
`testBehavior`（45 断言）：
- 默认名为 "paragraph"（branch）、永不 isLeaf、getAllowsChildren true；新 branch 无子时 getStartOffset 抛 NullPointerException（childless 反例）；replace 挂接子后区间为子并集 [0,6)。
- getElementIndex：命中首/次子、越界钳到末子。
- positionToElement 精确语义：子首字符命中、边界（子起点）归下一个子、末子内命中、恰好 endOffset 处 null（不钳）、负偏移 null、past-end null。
- toString 精确格式 "BranchElement(paragraph) 0,6\n"（换行结尾）。
- children() 枚举顺序且会耗尽；ElementNameAttribute="$ename" 属性可覆盖名字；未持锁 replace → StateInvariantError。
