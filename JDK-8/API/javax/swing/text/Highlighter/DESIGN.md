# javax.swing.text.Highlighter — DESIGN

## 1. API 功能
高亮标记接口，允许用彩色区域标记文档背景。方法分组：
- 生命周期：`install(JTextComponent)` / `deinstall(JTextComponent)` / `paint(Graphics)`。
- 高亮管理：`addHighlight(int,int,HighlightPainter)`（返回 tag，抛 `BadLocationException`）、`removeHighlight(Object)`、`removeAllHighlights()`、`changeHighlight(Object,int,int)`、`getHighlights()`（返回 `Highlight[]`）。

## 2. Java 用户如何使用
接口通常用 `DefaultHighlighter` 实现：`Highlighter h = new DefaultHighlighter(); h.install(area); Object t = h.addHighlight(1, 4, DefaultHighlighter.DefaultPainter); ... h.changeHighlight(t, 2, 5); h.removeHighlight(t);`。`JTextComponent.setHighlighter/getHighlighter` 关联组件。

## 3. 开源代码实际用法调研（来源：JDK 源码）
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/DefaultCaret.java`（`setSelectionVisible`/`handleMoveDot`）：`Highlighter h = component.getHighlighter(); ... selectionTag = h.addHighlight(p0, p1, p); ... h.changeHighlight(selectionTag, p0, p1); ... h.removeHighlight(selectionTag);` —— 选区高亮直接走 Highlighter 接口。
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/DefaultHighlighter.java`：实现全部 Highlighter 方法，`addHighlight` 对 `p0<0` 或 `p1<p0` 抛 `BadLocationException`。
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/DefaultHighlighter.java`（`getHighlights`）：空时返回共享空数组 `noHighlights`，否则拷贝 `Vector` 到 `Highlight[]`。

## 4. 测试用例设计 + 自动化实现
（24 断言）：
- 接口形状：install/deinstall/paint/addHighlight/removeHighlight/removeAllHighlights/changeHighlight/getHighlights。
- 嵌套面：Highlight 是接口（getStartOffset/getEndOffset/getPainter）；HighlightPainter 是接口且不继承任何接口（仅 paint）。
- DefaultHighlighter 可 headless 实例化：新实例无高亮；DefaultHighlightPainter 构造保留颜色。
- 反例：未 install 到组件就 removeAllHighlights → NPE（需要 JTextComponent，UI 面跳过）。
