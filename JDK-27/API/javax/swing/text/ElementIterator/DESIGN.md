# javax.swing.text.ElementIterator — DESIGN

## 1. API 功能
`ElementIterator implements Cloneable`，对 `Element` 树做深度优先遍历。方法分组：
- 构造器：`ElementIterator(Document)`（从 `document.getDefaultRootElement()` 开始）、`ElementIterator(Element)`（从指定根开始）。
- 遍历：`first()` 返回根元素并重置栈；`next()` 深度优先返回下一元素（结束返回 null）；`previous()` 返回上一元素（当前为根或已耗尽返回 null）；`current()` 返回当前元素。
- 查询/复制：`depth()` 返回当前栈深；`clone()` 复制迭代器（含遍历栈状态）。

## 2. Java 用户如何使用
`ElementIterator it = new ElementIterator(doc.getDefaultRootElement());` 然后 `Element e; while ((e = it.next()) != null) { ... }` 遍历整棵元素树；`first()`/`current()` 用于回到根/获取当前；`previous()` 用于后退。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/AbstractWriter.java`：构造 `new ElementIterator(doc.getDefaultRootElement())` 并 `it.next()` 逐元素写出。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/MinimalHTMLWriter.java`：通过 ElementIterator 遍历 DOM 元素树生成 HTML。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLDocument.java`：`ElementIterator` 用于遍历 HTML 元素层次。

## 4. 测试用例设计（黑盒部分，`ElementIteratorTest.testBlackBox`）
文档 `"hello\nworld"`（PlainDocument 默认根 = paragraph 分支，含 2 个 content 叶）：
1. `first()` == 默认根、`current()` == 根、`depth()` 从 0 → 1。
2. 深度优先 `next()` 依次返回 leaf0[0,6)、leaf1[6,12)、再 null（结束）。
3. 耗尽后 `previous()/current()` 均 null；中间状态 `previous()` 从首叶子返回根。
4. 未调用 first/current 直接 `next()` 返回根；Element 构造器等价；`clone()` 独立遍历且状态一致。

## 5. 契约深化（本轮追加）
`testDocumentIteration/testNavigation/testClone/testNullsAndExhaustion`（43 断言）：
- 类型面：abstract 类、方法面 first/current/next/previous/depth/clone。
- 深度优先序精确串：PlainDocument("ab\ncd\nef") → "paragraph@0 content@0 content@3 content@6 "；以根元素构造与以文档构造等价；styled 树 → "section paragraph content "。
- 导航语义：first 返回默认根、depth(root)==1；next 深入并推进 current；previous 返回父节点但不移动 current；previous 后 next 从原位继续；耗尽后 next/current 持续 null；first 可重启。
- clone 独立：新实例同起点，互不影响推进。
- 边界：ElementIterator((Element)null) 的 first/next/current/previous 全 null 且 depth 0；ElementIterator((Document)null) → NullPointerException。
