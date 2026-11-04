# javax.swing.text.AbstractDocument.AttributeContext — DESIGN

## 1. API 功能
接口，定义属性集不可变视图的构造与回收策略（`StyleContext` 是其标准实现）。方法分组：
- 增删：`addAttribute(AttributeSet,Object,Object)`、`addAttributes(AttributeSet,AttributeSet)`。
- 删：`removeAttribute(AttributeSet,Object)`、`removeAttributes(AttributeSet,Enumeration)`、`removeAttributes(AttributeSet,AttributeSet)`。
- 基础：`getEmptySet()`、`reclaim(AttributeSet)`（回收无引用集合的钩子）。

## 2. Java 用户如何使用
通常不直接面向用户，而是由 `AbstractDocument`/`DefaultStyledDocument` 内部持有（`StyleContext`）。可显式 `new StyleContext()` 后按接口调用：`AttributeContext ac = new StyleContext(); AttributeSet e = ac.getEmptySet(); AttributeSet a = ac.addAttribute(e, "k", "v"); ac.removeAttribute(a, "k");`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/swing/text/AbstractDocument.java:143`：`AbstractDocument(Content data, AttributeContext context)` 保存 `context`；`AbstractDocument(Content)` 默认传 `StyleContext.getDefaultStyleContext()`。
- `src/java.desktop/share/classes/javax/swing/text/StyleContext.java`：`StyleContext` 实现 `AbstractDocument.AttributeContext`，其 `getEmptySet()` 返回 `SimpleAttributeSet.EMPTY`，`addAttribute/removeAttribute` 基于 `SmallAttributeSet` 合并。
- `src/java.desktop/share/classes/javax/swing/text/DefaultStyledDocument.java:388`：`addStyle` 里 `StyleContext styles = (StyleContext) getAttributeContext(); return styles.addStyle(nm, parent);`。

## 4. 测试用例设计 + 自动化实现（`AttributeContextTest.testBlackBox`）
- 用 `new StyleContext()` 作为 `AttributeContext` 实现。
- `getEmptySet()` 非空、属性数为 0、实例稳定。
- `addAttribute/addAttributes` 合并结果可读；`removeAttribute/removeAttributes(AttributeSet)/removeAttributes(Enumeration)` 逐项删除。
- `reclaim` 可调用且不影响集合可用性。
