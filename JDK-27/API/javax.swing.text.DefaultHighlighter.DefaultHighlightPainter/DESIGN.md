# javax.swing.text.DefaultHighlighter.DefaultHighlightPainter — DESIGN

## 1. API 功能
`LayeredHighlighter.LayerPainter` 的具体实现，用纯色填充高亮区域。方法分组：
- 构造器：`DefaultHighlightPainter(Color)`（c 为 null 时绘制时取组件的选区色）。
- 颜色查询：`getColor()`。
- 绘制：`paint(Graphics,int,int,Shape,JTextComponent)`、`paintLayer(Graphics,int,int,Shape,JTextComponent,View)`（返回实际绘制的 Shape）。

## 2. Java 用户如何使用
`new DefaultHighlightPainter(Color.RED)` → 作为 painter 传给 `DefaultHighlighter.addHighlight(p0, p1, painter)`；`getColor()` 读回构造时颜色；`DefaultHighlighter.DefaultPainter` 是颜色为 null 的共享单例，绘制时使用 `JTextComponent.getSelectionColor()`。

## 3. 开源代码实际用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/DefaultHighlighter.java`：`public static final LayeredHighlighter.LayerPainter DefaultPainter = new DefaultHighlightPainter(null);` —— 静态共享实例。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/DefaultHighlighter.java`（`paint`）：`Color color = getColor(); if (color == null) { g.setColor(c.getSelectionColor()); } else { g.setColor(color); }` —— null 颜色回退到组件选区色。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/DefaultHighlighter.java`（`paintLayer`）：`Color color = getColor(); ... g.fillRect(r.x, r.y, r.width, r.height);` —— 分层绘制填充矩形。

## 4. 测试用例设计（黑盒部分，`DefaultHighlightPainterTest.testBlackBox`）
1. 构造带 Color：getColor 返回同一实例；null 构造：getColor 为 null。
2. 类型层级：instanceof HighlightPainter 与 LayerPainter。
3. `DefaultHighlighter.DefaultPainter` 非 null、类型为 DefaultHighlightPainter、颜色为 null。
4. 作为 painter 加入 DefaultHighlighter：getPainter 按同一性读回、偏移正确。
