# javax.swing.text.html.HTMLDocument.Iterator — DESIGN

## 1. API 功能
HTML 文档叶子元素迭代器（`HTMLDocument` 的嵌套 public abstract 类，`extends Object`，无接口）。核心方法分组：
- 查询：`getTag()`（当前标签）、`getAttributes()`、`getStartOffset()`、`getEndOffset()`。
- 遍历：`next()`（前进）、`isValid()`（是否仍有元素）。

## 2. Java 用户如何使用
`HTMLDocument.Iterator it = doc.getIterator(HTML.Tag.A);`（块级标签返回 null），随后 `while (it.isValid()) { it.getTag(); it.getAttributes().getAttribute(HTML.Attribute.HREF); it.next(); }` 遍历所有 A 标签。典型链：getIterator(A) → isValid → getAttributes → next。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLDocument.java:575`：`public Iterator getIterator(HTML.Tag t) { if (t.isBlock()) return null; return new LeafIterator(t, this); }` 块级标签返回 null。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLDocument.java:170`：类 javadoc 说明 `getIterator(HTML.Tag t)` 用于遍历指定标签。

## 4. 测试用例设计（黑盒部分，`IteratorTest.testBlackBox`）
1. `getIterator(A)` 非 null。
2. 遍历时 getTag()==A、getStartOffset()>=0、getEndOffset()>=start、getAttributes() 非 null 且 href=="http://example.com/"。
3. 恰好 1 个 <a>；耗尽后 getStartOffset()==-1、isValid()==false。
4. `getIterator(HTML.Tag.P)`（块级）== null。
5. 每次 `getIterator(A)` 返回新实例（非 null）。
