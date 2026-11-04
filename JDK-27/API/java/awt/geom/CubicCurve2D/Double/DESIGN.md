# java.awt.geom.CubicCurve2D.Double 设计案

## 1. API 功能分组
- **公有字段**：`x1,y1,ctrlx1,ctrly1,ctrlx2,ctrly2,x2,y2`（double）。
- **构造器**：`()`、`(double ×8)`。
- **坐标访问**：`getX1/getY1/getP1/getCtrlX1/getCtrlY1/getCtrlP1/getCtrlX2/getCtrlY2/getCtrlP2/getX2/getY2/getP2`。
- **设置**：`setCurve(double…)`、继承 `setCurve(CubicCurve2D)` 等。
- **继承**：CubicCurve2D 全部行为。

## 2. Java 用户如何使用
```
CubicCurve2D.Double c = new CubicCurve2D.Double(0,0,10,20,30,-20,40,0);
double flat = c.getFlatness();
```

## 3. 开源代码实际用法调研（来源标注）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/geom/CubicCurve2D.java`：Double 嵌套类。
- `~/git/jdk25u-dev/test/jdk/java/awt/geom/CubicCurve2D/SolveCubicTest.java`：solveCubic。
- JDK `CubicIterator.java`：iterator。

## 4. 测试用例设计 + 自动化实现
黑盒（`DoubleTest.testBlackBox()`，30–60 断言）：
1. 默认零构造、8-double 构造。
2. 公有字段读取/写入。
3. getP1/getCtrlP1/getCtrlP2/getP2。
4. setCurve(double)。
5. getFlatness、getBounds2D。
6. iterator MOVETO+CUBICTO 8 坐标。
白盒：类形态、superclass=CubicCurve2D、implements Serializable、公有 double 字段、2 个构造器。
