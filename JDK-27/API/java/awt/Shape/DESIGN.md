# java.awt.Shape 黑盒行为测试设计

## 1. API 功能
- 接口（所有几何形状的统一抽象）。
- 包围盒：`getBounds()`（`Rectangle`）、`getBounds2D()`（`Rectangle2D`）。
- 命中/包含：`contains(double,double)`、`contains(Point2D)`、`contains(double,double,double,double)`、`contains(Rectangle2D)`。
- 相交：`intersects(double,double,double,double)`、`intersects(Rectangle2D)`。
- 路径：`getPathIterator(AffineTransform)`、`getPathIterator(AffineTransform,double)`。

## 2. Java 用户如何使用
`Graphics2D.draw/fill`、`setClip` 与命中测试都面向 `Shape` 抽象，任何实现（Rectangle/Polygon/Ellipse2D/Path2D）可互换：
```java
Shape s = new Rectangle(0, 0, 100, 100);
g2.draw(s);
if (s.contains(x, y)) { ... }
PathIterator it = s.getPathIterator(null);
```
组合链路：取实现为 `Shape` → `getBounds` → `contains/intersects` → `getPathIterator`。

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 `src/java.desktop/share/classes/java/awt/Shape.java`：接口方法声明（返回类型 `Rectangle`/`Rectangle2D`/`boolean`/`PathIterator`）。
- JDK 自带测试（真实用法）：`test/jdk/java/awt/geom/HitTest/PathHitTest.java`（Shape 命中测试）、`test/jdk/java/awt/Graphics2D/RenderClipTest/RenderClipTest.java`（Shape 作为 clip）。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- 以 `Rectangle` 为 `Shape`：`getBounds`/`getBounds2D`、`contains`（double/Point2D/矩形重载）、`intersects`（double/Rectangle2D）、`getPathIterator`（`WIND_NON_ZERO`、首段 `SEG_MOVETO`）。
- 以 `Polygon` 为 `Shape`：`getBounds`、`contains`、`getPathIterator`（`WIND_EVEN_ODD`）。
断言 27 项，几何精确断言（headless-safe）。


## 5. 深化补充（testContract，JDK-8+JDK-27 双侧对齐）
本任务在保留原有断言基础上追加契约级深化：
testContract：接口方法存在；跨实现 contains/intersects 一致；Polygon contains；段数相同（矩形 6 段）。
全部为 headless（env -u DISPLAY）实测锁定值，双端 JDK 行为一致。
