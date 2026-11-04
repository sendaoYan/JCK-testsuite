# java.awt.geom.Line2D 设计案

## 1. API 功能分组
- **坐标访问**：`getX1/getY1/getP1/getX2/getY2/getP2`（抽象）。
- **设置**：`setLine(double x1,y1,x2,y2)`（抽象）、`setLine(Point2D,Point2D)`、`setLine(Line2D)`。
- **几何关系（静态+实例）**：`relativeCCW`、`linesIntersect`、`ptSegDistSq/ptSegDist`（线段距离）、`ptLineDistSq/ptLineDist`（无限直线距离）。
- **Shape 协议**：`contains`、`intersects`、`getBounds()/getBounds2D()`、`getPathIterator`、`clone`。

## 2. Java 用户如何使用
```
Line2D line = new Line2D.Double(0, 0, 10, 0);
double d = line.ptSegDist(5, 3);          // 点到线段最近距离
boolean cross = Line2D.linesIntersect(0,0,2,2, 0,2,2,0);
int side = Line2D.relativeCCW(0,0,1,0, px, py);  // 点在有向线哪一侧
```
`ptSegDist` 与 `ptLineDist` 的区别：前者把线段端点当边界、后者把线段延长为无限直线。

## 3. 开源代码实际用法调研（来源标注）
- `~/git/jdk25u-dev/test/jdk/java/awt/geom/Line2D/NegLineDistSqBug.java`：ptLineDistSq/ptSegDistSq 负值边界。
- `~/git/jdk25u-dev/test/jdk/java/awt/geom/Line2D/AALineTest.java`：绘制用线。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/geom/Line2D.java`：relativeCCW 叉积实现（px*y2 - py*x2）。
- JDK 内部 `LineIterator.java` 用于把 Line2D 转成 PathIterator。

## 4. 测试用例设计 + 自动化实现
黑盒（`Line2DTest.testBlackBox()`，经 Line2D.Double，15–25 断言）：
1. relativeCCW：y-down 下有向线 (0,0)→(1,0) 的左右判定与共线（on/超出端点）分类。
2. linesIntersect：相交/不相交。
3. ptSegDist/ptSegDistSq 垂距；ptSegDist 在投影超出端点时钳到端点，ptLineDist 用无限直线。
4. 实例 relativeCCW(Point2D/double)。
5. getBounds/getBounds2D、getPathIterator（MOVETO+LINETO）。
6. contains(point) 恒 false、intersects 矩形。
7. clone 独立。
白盒：抽象类、implements Shape/Cloneable、protected 构造器、静态几何方法集。
