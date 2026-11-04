# javax.swing.text.AbstractDocument.AbstractElement — DESIGN

## 1. API 功能
抽象元素基类，同时实现 `Element`、`MutableAttributeSet`、`Serializable`、`TreeNode`。方法分组：
- 元素结构（abstract）：`getStartOffset/getEndOffset/getElement/getElementCount/getElementIndex/isLeaf`。
- 元素关系：`getDocument()`、`getParentElement()`、`getAttributes()`、`getName()`。
- 属性集（MutableAttributeSet）：`getAttributeCount/isDefined/isEqual/copyAttributes/getAttribute/getAttributeNames/containsAttribute/containsAttributes/getResolveParent/addAttribute/addAttributes/removeAttribute/removeAttributes/setResolveParent`。
- TreeNode：`getChildAt/getChildCount/getParent/getIndex/getAllowsChildren/children`。
- 调试：`dump(PrintStream,int)`。

## 2. Java 用户如何使用
通过文档元素树拿到实例：`PlainDocument doc; doc.insertString(0,"ab\ncd",null); Element leaf = doc.getDefaultRootElement().getElement(0);` 然后调用 `leaf.getStartOffset()/getEndOffset()/getName()/isLeaf()` 等只读方法；修改属性（`addAttribute/removeAttribute/...`）需在持有文档写锁（`writeLock`）时进行，否则抛 `StateInvariantError`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/swing/text/AbstractDocument.java:1800`：`public abstract class AbstractElement implements Element, MutableAttributeSet, Serializable, TreeNode`；`BranchElement`/`LeafElement` 均继承它。
- `src/java.desktop/share/classes/javax/swing/text/PlainDocument.java:151`：`createDefaultRoot()` 用 `createBranchElement/createLeafElement` 构造 `BranchElement map` 并 `map.replace(0,0,lines)`。
- `test/jdk/javax/swing/text/WrappedPlainView/6857057/StubBranchElement.java`、`StubLeafElement.java`：测试子类化 `BranchElement`/`LeafElement` 以校验视图对元素的消费。

## 4. 测试用例设计 + 自动化实现（`AbstractElementTest.testBlackBox`）
- 经 `PlainDocument` 元素树验证只读面：`getDocument/getParentElement/getAttributes/getName/getStartOffset/getEndOffset/isLeaf/getElementCount/getElement/getElementIndex`。
- TreeNode 面：`getChildCount/getChildAt/getParent/getIndex/getAllowsChildren/children` 枚举。
- 用暴露 `writeLock` 的 `AbstractDocument` 子类在写锁内验证 `MutableAttributeSet` 面：`addAttribute/getAttribute/isDefined/containsAttribute/containsAttributes/copyAttributes/isEqual/removeAttribute/addAttributes/removeAttributes(Enumeration/AttributeSet)/setResolveParent/getResolveParent/getAttributeNames`。

## 5. 契约深化（本轮追加）
`testAttributeContract/testResolveParent/testLockGuard`（55 断言）：
- getAttributes() 返回元素自身（identity）；新元素无属性且与 SimpleAttributeSet.EMPTY isEqual。
- 属性契约（写锁内经暴露 writeLock 的子类验证）：add/get/isDefined/getAttributeNames 枚举、同键覆写；copyAttributes 为独立快照（改元素后旧快照不变、不 isEqual）；containsAttribute/containsAttributes 值精确。
- resolve parent：默认解析到父元素属性（穿透查找），显式 setResolveParent 覆盖；removeAttributes(this) 清空自身键。
- 锁卫反例：未持写锁调用 addAttribute → javax.swing.text.StateInvariantError("Illegal cast to MutableAttributeSet")（包私有类，用类名比较断言）。
- TreeNode 面：getChildAt/getChildCount/getParent/getIndex/getAllowsChildren/children 经 AbstractElement 引用可用。
