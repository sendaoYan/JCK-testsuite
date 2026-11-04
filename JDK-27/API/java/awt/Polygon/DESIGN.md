# java.awt.Polygon 黑盒行为测试设计

## 1. API 功能
- 构造：`Polygon()`、`Polygon(int[] xpoints, int[] ypoints, int npoints)`。
- 公开字段：`npoints`、`xpoints`、`ypoints`。
- 修改：`addPoint(int,int)`（自动扩容）、`translate(int,int)`、`reset`、`invalidate`。
- 查询/几何：`getBounds`、`getBoundingBox`、`getBounds2D`、`contains(Point)`、`contains(int,int)`、`inside(int,int)`、`contains(double,double)`、`contains(Point2D)`、`intersects(double,double,double,double)`、`intersects(Rectangle2D)`、`contains(double,double,double,double)`、`contains(Rectangle2D)`、`getPathIterator(AffineTransform[,double])`。

## 2. Java 用户如何使用
自定义多边形命中区域与绘制：`g.fillPolygon(poly)`，`poly.contains(x,y)` 判定点是否在区域，`getBounds` 求包围盒。
```java
Polygon poly = new Polygon(new int[]{0,10,10,0}, new int[]{0,0,10,10}, 4);
if (poly.contains(mx, my)) { ... }
poly.translate(5, 5);
Rectangle b = poly.getBounds();
```
组合链路：构造 → `addPoint`/`translate` → `getBounds` → `contains`/`intersects` → `getPathIterator`。

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 `src/java.desktop/share/classes/java/awt/Polygon.java`：数组构造器拷贝前 npoints 个点（`Arrays.copyOf`，截断到 npoints 长度）；`npoints > length` 抛 `IndexOutOfBoundsException`，`npoints<0` 抛 `NegativeArraySizeException`；`addPoint` 容量翻倍（MIN_LENGTH=4）；`getPathIterator` 用 `WIND_EVEN_ODD`。
- JDK 自带测试（真实用法）：`test/jdk/java/awt/Graphics2D/Headless/HeadlessPolygon.java`（headless 下 `new Polygon` + 几何）、`test/jdk/java/awt/Graphics2D/RenderClipTest/RenderClipTest.java`（`new Polygon` + 绘制）。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- 默认构造（npoints=0、数组长度 4、空包围盒）。
- 数组构造器拷贝与截断、参数不别名、越界/负 npoints 异常。
- `getBounds/getBoundingBox/getBounds2D`。
- `contains` 点/`Point`/`inside`/double/`Point2D`/矩形重载 + 内外边界。
- `intersects`（int/Rectangle2D 重载）。
- `translate` 平移所有顶点与包围盒。
- `addPoint` 递增 + 容量增长 + 新顶点存储 + 包围盒更新。
- `reset`/`invalidate`。
- `getPathIterator`：`WIND_EVEN_ODD`、首段 `SEG_MOVETO`、后续 `SEG_LINETO`、段数（MOVETO+2*LINETO+CLOSE）、带 `AffineTransform` 的坐标变换。
断言 67 项，几何精确断言（headless-safe）。


## 5. 深化补充（testContract，JDK-8+JDK-27 双侧对齐）
本任务在保留原有断言基础上追加契约级深化：
testContract：构造器数组拷贝；容量 20 点→32；越界/负数异常精确；contains 边界锁定((5,0)=true,(0,0)=true,(10,10)=false)；bounds 缓存；translate/reset；getBounds2D；序列化。
全部为 headless（env -u DISPLAY）实测锁定值，双端 JDK 行为一致。
