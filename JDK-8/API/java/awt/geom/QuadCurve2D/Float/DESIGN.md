# java.awt.geom.QuadCurve2D.Float 设计案

## 1. API 功能分组
- **公有字段**：`x1,y1,ctrlx,ctrly,x2,y2`（float）。
- **构造器**：`()`、`(float x1,y1,ctrlx,ctrly,x2,y2)`。
- **坐标访问**：`getX1/getY1/getP1/getCtrlX/getCtrlY/getCtrlPt/getX2/getY2/getP2`。
- **设置**：`setCurve(double…)`、`setCurve(float…)`、继承 `setCurve(double[],int)` 等。
- **继承**：QuadCurve2D 的 getFlatness/subdivide/solveQuadratic/contains/getBounds2D/getPathIterator。

## 2. Java 用户如何使用
```
QuadCurve2D.Float q = new QuadCurve2D.Float(0f,0f,10f,10f,20f,0f);
double flat = q.getFlatness();
Rectangle2D b = q.getBounds2D();
```
字段公开可直接读写；setCurve 提供 float/double 两套。

## 3. 开源代码实际用法调研（来源标注）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/geom/QuadCurve2D.java`：Float 嵌套类。
- `~/git/jdk25u-dev/test/jdk/java/awt/geom/QuadCurve2D/Quad2DContainsTest.java`：contains。
- JDK `QuadIterator.java`：iterator 表示。

## 4. 测试用例设计 + 自动化实现
黑盒（`FloatTest.testBlackBox()`，30–60 断言）：
1. 默认零构造、6-float 构造。
2. 公有字段读取/写入。
3. getP1/getCtrlPt/getP2。
4. setCurve(float/double/继承 QuadCurve2D 重载)。
5. getFlatness/getFlatnessSq、getBounds2D、contains 内外。
6. iterator MOVETO+QUADTO 坐标。
白盒：类形态、superclass=QuadCurve2D、implements Serializable、公有 float 字段、2 个构造器。
