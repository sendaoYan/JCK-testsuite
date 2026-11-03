# java.awt.geom.QuadCurve2D.Double 设计案

## 1. API 功能分组
- **公有字段**：`x1,y1,ctrlx,ctrly,x2,y2`（double）。
- **构造器**：`()`、`(double x1,y1,ctrlx,ctrly,x2,y2)`。
- **坐标访问**：`getX1/getY1/getP1/getCtrlX/getCtrlY/getCtrlPt/getX2/getY2/getP2`。
- **设置**：`setCurve(double…)`、继承 `setCurve(QuadCurve2D)` 等。
- **继承**：QuadCurve2D 全部行为。

## 2. Java 用户如何使用
```
QuadCurve2D.Double q = new QuadCurve2D.Double(0,0,10,10,20,0);
double flat = q.getFlatness();
```
高精度二次贝塞尔，字段公开。

## 3. 开源代码实际用法调研（来源标注）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/geom/QuadCurve2D.java`：Double 嵌套类。
- `~/git/jdk25u-dev/test/jdk/java/awt/geom/QuadCurve2D/Quad2DContainsTest.java`：contains。
- JDK `QuadIterator.java`：iterator。

## 4. 测试用例设计 + 自动化实现
黑盒（`DoubleTest.testBlackBox()`，30–60 断言）：
1. 默认零构造、6-double 构造。
2. 公有字段读取/写入。
3. getP1/getCtrlPt/getP2。
4. setCurve(double/继承 QuadCurve2D)。
5. getFlatness/getFlatnessSq、getBounds2D、contains 内外。
6. iterator MOVETO+QUADTO 坐标。
白盒：类形态、superclass=QuadCurve2D、implements Serializable、公有 double 字段、2 个构造器。
