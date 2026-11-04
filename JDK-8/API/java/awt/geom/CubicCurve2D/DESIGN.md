# java.awt.geom.CubicCurve2D 设计案

## 1. API 功能分组
- **坐标访问**：`getX1/getY1/getP1`、`getCtrlX1/getCtrlY1/getCtrlP1`、`getCtrlX2/getCtrlY2/getCtrlP2`、`getX2/getY2/getP2`（抽象）。
- **设置**：`setCurve(double…)`（抽象）、`setCurve(double[],int)`、`setCurve(Point2D×4)`、`setCurve(Point2D[],int)`、`setCurve(CubicCurve2D)`。
- **平坦度**：`getFlatnessSq()/getFlatness()`（实例）+ 静态 `getFlatnessSq/getFlatness(double[],int)`。
- **细分**：`subdivide(CubicCurve2D,CubicCurve2D)`（实例）+ 静态 3 参/数组版。
- **求根**：`solveCubic(double[])`、`solveCubic(double[],double[])`。
- **Shape**：`contains/intersects/getBounds2D/getBounds/getPathIterator`、`clone`。

## 2. Java 用户如何使用
```
CubicCurve2D c = new CubicCurve2D.Double(0,0,10,20,30,-20,40,0);
double flat = c.getFlatness();
CubicCurve2D l = new CubicCurve2D.Double(), r = new CubicCurve2D.Double();
c.subdivide(l, r);
double[] roots = new double[3];
int n = CubicCurve2D.solveCubic(eqn, roots);   // 三次方程求根
```

## 3. 开源代码实际用法调研（来源标注）
- `~/git/jdk25u-dev/test/jdk/java/awt/geom/CubicCurve2D/SolveCubicTest.java`：solveCubic。
- `~/git/jdk25u-dev/test/jdk/java/awt/geom/CubicCurve2D/ContainsTest.java`：contains。
- `~/git/jdk25u-dev/test/jdk/java/awt/geom/CubicCurve2D/IntersectsTest.java`：intersects。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/geom/CubicCurve2D.java`：subdivide/solveCubic。
- JDK `CubicIterator.java`：iterator 表示。

## 4. 测试用例设计 + 自动化实现
黑盒（`CubicCurve2DTest.testBlackBox()`，经 CubicCurve2D.Double，15–25 断言）：
1. 平坦度实例+静态（控制点 y=±20 → 20/400）。
2. subdivide 实例/静态/数组，断言左右控制点精确值。
3. solveCubic 根 {d,c,b,a} 系数序，排序断言 1/2/3。
4. setCurve 全部重载。
5. getBounds2D：JDK-8 返回控制点凸包（y∈[-20,20] 高 40）；JDK-9+ 起改为精确曲线包围盒（本曲线 y∈[-5.77,5.77] 高 11.547）——版本差异，JDK-27 侧断言精确包围盒。intersects。
6. getPathIterator(flatness) 细分、clone。
白盒：抽象类、implements Shape/Cloneable、protected 构造器、静态平坦度/细分/求根方法。
