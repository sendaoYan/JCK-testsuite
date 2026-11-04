# java.awt.GradientPaint 黑盒行为测试设计

## 1. API 功能
- 构造：`(float,float,Color,float,float,Color)`、`(Point2D,Color,Point2D,Color)`、及各自带 `boolean cyclic` 的变体。
- 查询：`getPoint1`（返回副本）、`getColor1`、`getPoint2`（返回副本）、`getColor2`、`isCyclic`。
- 度量：`getTransparency`（两色均 opaque→OPAQUE，否则 TRANSLUCENT）。
- 绘制：`createContext(ColorModel,Rectangle,Rectangle2D,AffineTransform,RenderingHints)` 返回 `PaintContext`。

## 2. Java 用户如何使用
`Graphics2D.setPaint` 的渐变填充：两点锚定颜色，沿连线线性渐变；`cyclic=true` 周期重复。
```java
GradientPaint gp = new GradientPaint(0, 0, Color.RED, 100, 100, Color.BLUE);
g2.setPaint(gp);
g2.fill(shape);
```
组合链路：构造 → `getPoint1/getColor1` → `getTransparency` → `createContext` → 取 `Raster` → `dispose`。

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 `src/java.desktop/share/classes/java/awt/GradientPaint.java`：`getPoint1/getPoint2` 每次返回新 `Point2D.Float` 副本；构造器对 null 颜色/点抛 `NullPointerException`；`getTransparency` 用 `(a1 & a2)==0xff`。
- JDK 自带测试（真实用法）：`test/jdk/java/awt/Graphics2D/DrawString/TextRenderingTest.java`（`new GradientPaint`）、`test/jdk/java/awt/Graphics/XORPaint.java`（`new GradientPaint` 填充）。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- float 构造器（acyclic）+ 各 getter 精确值；`getPoint1/getPoint2` 返回独立副本。
- Point2D 构造器坐标。
- cyclic 两种构造器。
- null 颜色/点抛 `NullPointerException`。
- `getTransparency` OPAQUE/TRANSLUCENT 两分支。
- `createContext` 非空 + `getRaster` 尺寸 + `dispose` 可调用。
断言 34 项，几何/字段精确断言（headless-safe）。
