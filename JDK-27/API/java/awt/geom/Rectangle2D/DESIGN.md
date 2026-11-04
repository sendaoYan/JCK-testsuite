# java.awt.geom.Rectangle2D 设计案

## 1. API 功能分组
- **构造/设框**：抽象 `setRect(double,double,double,double)`、`setRect(Rectangle2D)`、继承 `setFrame`。
- **空间关系**：`outcode(double,double)/outcode(Point2D)`（OUT_LEFT=1/OUT_TOP=2/OUT_RIGHT=4/OUT_BOTTOM=8 位标志）、`contains`、`intersects`、`intersectsLine`。
- **集合运算**：实例 `createIntersection/createUnion`；静态 `intersect(src1,src2,dst)`/`union(src1,src2,dst)`。
- **扩展**：`add(double,double)/add(Point2D)/add(Rectangle2D)` 把点/矩形并入最小外接框。
- **工具**：`equals/hashCode`。

## 2. Java 用户如何使用
`Rectangle2D r = new Rectangle2D.Double(x, y, w, h);` 做命中/裁剪：`if (r.contains(px, py)) ...`；`r.intersects(area)` 判重叠；`r.createUnion(other)` 求并集。批量：`Rectangle2D.intersect(a, b, dest)` 复用目标对象避免分配。

## 3. 开源代码实际用法调研
- `java.awt.geom.Rectangle2D.Double/Float` 是 AWT 布局/绘制最常用矩形（`Graphics2D.clipRect` 内部用）。
- `test/jdk/java/awt/geom/Rectangle2D/` 系列（如 `IntersectsLineHang`、`ContainsTest`、`OutcodeTest`）分别覆盖 `intersectsLine`、`contains`、`outcode` 边界（本用例 outcode/contains/intersectsLine 场景参考其主题）。
- `java.awt.geom.Rectangle2D` 的 `intersect/union` 静态方法在 `sun.java2d` 裁剪路径中被使用。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`Rectangle2DTest.testBlackBox()`，用 Rectangle2D.Double）：
1. outcode 常量值与四个方向判定；contains 边界（含左/上、不含右/下）。
2. intersects 重叠/分离；intersectsLine 相交/外部。
3. 静态 intersect/union 与实例 createIntersection/createUnion；disjoint 交集为空。
4. add 家族（double,double / Point2D / Rectangle2D）扩展最小外接框。
5. setRect(Rectangle2D)、setFrame、equals/hashCode。
白盒（`testWhiteBox()`）：abstract extends RectangularShape、抽象 setRect/outcode/createIntersection/createUnion、静态 intersect/union 签名。
