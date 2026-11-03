# javax.swing.plaf.nimbus.AbstractRegionPainter — DESIGN

## 1. API 功能
`AbstractRegionPainter implements Painter<JComponent>`（public abstract 类），区域画家基类。`public final void paint(Graphics2D, JComponent, int, int)` 编排 `getPaintContext()`/`doPaint`；protected 抽象 `getPaintContext()` 与 `doPaint(Graphics2D, JComponent, int, int, Object[])`；protected final 工具 `decodeX/decodeY/decodeColor/decodeGradient` 等。来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/nimbus/AbstractRegionPainter.java`（class=43、paint=144、getPaintContext=188、doPaint=222）。

## 2. Java 用户如何使用
匿名/命名子类实现两个抽象方法：`new AbstractRegionPainter(){ protected PaintContext getPaintContext(){...} protected void doPaint(Graphics2D g, JComponent c, int w, int h, Object[] ext){...} }`，然后 `painter.paint(g, comp, w, h)`。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/nimbus/AbstractRegionPainter.java:144`：`paint` 先取 `getPaintContext()` 并校验 cacheMode。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/nimbus/AbstractRegionPainter.java:222`：`doPaint` 为抽象绘制点。

## 4. 测试用例设计（黑盒部分，`AbstractRegionPainterTest.testBlackBox`）
1. 子类实现 `doPaint` 置标记、`getPaintContext` 返回 null。
2. `paint(g, c, 0, 50)`（零宽）跳过 `doPaint`（标记仍 false）。
3. `paint(g, c, 100, 50)` 调用 `doPaint`（标记 true）。
