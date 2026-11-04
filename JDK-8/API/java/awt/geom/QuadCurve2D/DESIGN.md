# java.awt.geom.QuadCurve2D 设计案

## 1. API 功能分组
- **坐标访问**：`getX1/getY1/getP1`、`getCtrlX/getCtrlY/getCtrlPt`、`getX2/getY2/getP2`（抽象）。
- **设置**：`setCurve(double…)`（抽象）、`setCurve(double[],int)`、`setCurve(Point2D×3)`、`setCurve(Point2D[],int)`、`setCurve(QuadCurve2D)`。
- **平坦度**：`getFlatnessSq()/getFlatness()`（实例）+ `getFlatnessSq/getFlatness(double[],int)`（静态）。
- **细分**：`subdivide(QuadCurve2D,QuadCurve2D)`（实例）、`subdivide(QuadCurve2D,QuadCurve2D,QuadCurve2D)`（静态）、`subdivide(double[],int,double[],int,double[],int)`（静态数组）。
- **求根**：`solveQuadratic(double[])`、`solveQuadratic(double[],double[])`。
- **Shape**：`contains/intersects/getBounds2D/getBounds/getPathIterator`、`clone`。

## 2. Java 用户如何使用
```
QuadCurve2D q = new QuadCurve2D.Double(0,0,10,10,20,0);
double flat = q.getFlatness();                 // 控制点离弦最大距离
QuadCurve2D left = new QuadCurve2D.Double(), right = new QuadCurve2D.Double();
q.subdivide(left, right);                       // t=0.5 一分为二
double[] roots = new double[2];
int n = QuadCurve2D.solveQuadratic(eqn, roots); // 二次方程求根
```

## 3. 开源代码实际用法调研（来源标注）
- `~/git/jdk25u-dev/test/jdk/java/awt/geom/QuadCurve2D/Quad2DContainsTest.java`：contains 命中。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/geom/QuadCurve2D.java`：subdivide/solveQuadratic 实现。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/geom/QuadIterator.java`：QuadCurve2D 的 iterator。
- JDK `FlatteningPathIterator.java`：用 getFlatness 决定扁平化细分。

## 4. 测试用例设计 + 自动化实现
黑盒（`QuadCurve2DTest.testBlackBox()`，经 QuadCurve2D.Double，15–25 断言）：
1. 平坦度实例+静态（(0,0),(10,10),(20,0) → 10/100）。
2. subdivide 实例/静态/数组，断言左右控制点精确值。
3. solveQuadratic 根（系数 {c,b,a}），排序后断言。
4. setCurve 全部重载。
5. contains 透镜内/外、intersects、getBounds2D：JDK-8 返回控制点凸包（y∈[0,10] 高 10）；JDK-9+ 起改为精确曲线包围盒（本曲线高 5.000000000000007）——版本差异，JDK-27 侧断言精确包围盒。
6. getPathIterator(flatness) 细分、clone。
白盒：抽象类、implements Shape/Cloneable、protected 构造器、静态平坦度/细分/求根方法。
