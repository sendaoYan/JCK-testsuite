# javax.swing.text.LayeredHighlighter.LayerPainter — DESIGN

## 1. API 功能
分层高亮绘制器抽象类（implements `Highlighter.HighlightPainter`）。方法分组：
- 构造器：`protected LayerPainter()`。
- 抽象方法：`paintLayer(Graphics,int,int,Shape,JTextComponent,View)`，返回实际绘制的 Shape。
- 继承自 HighlightPainter：`paint(Graphics,int,int,Shape,JTextComponent)`。

## 2. Java 用户如何使用
实现 `paintLayer` 返回填充区域 Shape，并把该绘制器作为 `Highlighter.addHighlight` 的 painter。当 `DefaultHighlighter.drawsLayeredHighlights` 为 true 时走分层绘制（在文本前绘制背景）。可直接用 `DefaultHighlighter.DefaultHighlightPainter`。

## 3. 开源代码实际用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/LayeredHighlighter.java`：`public abstract static class LayerPainter implements Highlighter.HighlightPainter { public abstract Shape paintLayer(...); }`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/DefaultHighlighter.java`（`DefaultHighlightPainter.paintLayer`）：`Shape shape = view.modelToView(offs0, ...); r = (shape instanceof Rectangle) ? (Rectangle)shape : shape.getBounds(); ... g.fillRect(r.x, r.y, r.width, r.height); return r;` —— 用 View 的 modelToView 计算填充矩形并返回。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/DefaultHighlighter.java`（`addHighlight`）：`(p instanceof LayeredHighlighter.LayerPainter)` 时使用 `LayeredHighlightInfo`。

## 4. 测试用例设计（黑盒部分，`LayerPainterTest.testBlackBox`）
1. 匿名实现 paintLayer 返回固定 Shape，断言返回同一 Shape、instanceof HighlightPainter。
2. `DefaultHighlighter.DefaultHighlightPainter` instanceof LayerPainter。
3. 作为 painter 加入 DefaultHighlighter：数量正确、分层绘制开启。
