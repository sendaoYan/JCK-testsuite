# javax.swing.text.Highlighter — DESIGN

## 1. API 功能
高亮标记接口，允许用彩色区域标记文档背景。方法分组：
- 生命周期：`install(JTextComponent)` / `deinstall(JTextComponent)` / `paint(Graphics)`。
- 高亮管理：`addHighlight(int,int,HighlightPainter)`（返回 tag，抛 `BadLocationException`）、`removeHighlight(Object)`、`removeAllHighlights()`、`changeHighlight(Object,int,int)`、`getHighlights()`（返回 `Highlight[]`）。

## 2. Java 用户如何使用
接口通常用 `DefaultHighlighter` 实现：`Highlighter h = new DefaultHighlighter(); h.install(area); Object t = h.addHighlight(1, 4, DefaultHighlighter.DefaultPainter); ... h.changeHighlight(t, 2, 5); h.removeHighlight(t);`。`JTextComponent.setHighlighter/getHighlighter` 关联组件。

## 3. 开源代码实际用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/DefaultCaret.java`（`setSelectionVisible`/`handleMoveDot`）：`Highlighter h = component.getHighlighter(); ... selectionTag = h.addHighlight(p0, p1, p); ... h.changeHighlight(selectionTag, p0, p1); ... h.removeHighlight(selectionTag);` —— 选区高亮直接走 Highlighter 接口。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/DefaultHighlighter.java`：实现全部 Highlighter 方法，`addHighlight` 对 `p0<0` 或 `p1<p0` 抛 `BadLocationException`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/DefaultHighlighter.java`（`getHighlights`）：空时返回共享空数组 `noHighlights`，否则拷贝 `Vector` 到 `Highlight[]`。

## 4. 测试用例设计（黑盒部分，`HighlighterTest.testBlackBox`）
1. 初始 getHighlights 为空；install 后 addHighlight 一个区间，断言数量/偏移/painter。
2. changeHighlight 更新偏移；removeHighlight 删除。
3. 连续 add 多个后 removeAllHighlights 清空。
4. addHighlight 非法区间（负起点、终点小于起点）抛 BadLocationException。
