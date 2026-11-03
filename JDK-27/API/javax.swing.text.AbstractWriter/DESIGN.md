# javax.swing.text.AbstractWriter — DESIGN

## 1. API 功能
`AbstractWriter` 抽象类，把元素树/文档文本写到 `Writer`。公有方法分组：
- 查询偏移：`getStartOffset()`/`getEndOffset()`（构造时确定输出区间）。
- 换行符：`setLineSeparator(String)`/`getLineSeparator()`（默认来自文档属性或 `System.lineSeparator()`）。
受保护方法（子类使用）：`getText(Element)`、`text(Element)`、`write(...)`、`writeLineSeparator()`、`indent()`、`getElementIterator()`、`getWriter()`、`getDocument()`、抽象 `write()`。

## 2. Java 用户如何使用
继承 `AbstractWriter`，实现 `write()`，内部用 `getElementIterator()` 遍历元素并对叶子调用 `text(e)` 写出文本；通过 `setLineSeparator` 控制换行，`getStartOffset()/getEndOffset()` 得到写入区间。`MinimalHTMLWriter`/`HTMLWriter` 是其 JDK 内建子类。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/MinimalHTMLWriter.java`：`public class MinimalHTMLWriter extends AbstractWriter { ... super(w, doc); ... }`，在 `write()` 中调用 `indent()`/`text(elem)` 生成 HTML。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLWriter.java`：`super(w, doc, pos, len)` 按区间写文档片段。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/AbstractWriter.java`：`text(Element)` 用 `Math.max(getStartOffset(), ...)`/`Math.min(getEndOffset(), ...)` 裁剪到区间。

## 4. 测试用例设计（黑盒部分，`AbstractWriterTest.testBlackBox`）
1. 默认构造：`getStartOffset()==0`、`getEndOffset()==doc.getLength()`、`getDocument()` 身份、`getElementIterator().first()` 为默认根。
2. `getLineSeparator()` 默认 == `System.lineSeparator()`；`setLineSeparator/getLineSeparator` 往返。
3. 区间构造 `(w, doc, 1, 4)` → start=1、end=5。
4. 遍历叶子调用 `text(e)` 写回 `StringWriter`，输出 == 文档文本（无显示依赖）。
5. `getText(leaf)` 返回叶子文本；零长度区间不写任何内容。
