# java.awt.geom.Line2D.Float 设计案

## 1. API 功能分组
- **公有字段**：`x1,y1,x2,y2`（float）。
- **构造器**：`()`、`(float x1,y1,x2,y2)`、`(Point2D,Point2D)`。
- **坐标访问**：`getX1/getY1/getP1/getX2/getY2/getP2`。
- **设置**：`setLine(double…)`、`setLine(float…)`、继承 `setLine(Point2D,Point2D)`、`setLine(Line2D)`。
- **几何**：`getBounds2D()`；继承 Line2D 的距离/相交/contains/iterator。

## 2. Java 用户如何使用
```
Line2D.Float l = new Line2D.Float(0f, 0f, 10f, 0f);
l.setLine(1f, 2f, 3f, 4f);
double d = l.ptSegDist(5, 3);
Rectangle2D b = l.getBounds2D();
```
字段公开，可直接 `l.x1 = ...` 修改，也可用 setLine 系列。

## 3. 开源代码实际用法调研（来源标注）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/geom/Line2D.java`：Float 嵌套类字段与 setLine 重载。
- `~/git/jdk25u-dev/test/jdk/java/awt/geom/Line2D/AALineTest.java`：line 绘制。
- JDK `LineIterator.java`：Line2D.Float/Double 统一走 PathIterator。

## 4. 测试用例设计 + 自动化实现
黑盒（`FloatTest.testBlackBox()`，30–60 断言）：
1. 默认零构造、4-float 构造、Point2D 构造。
2. 公有字段直接读取与写入。
3. getX1..getY2/getP1/getP2。
4. setLine(float/double/Point2D,Point2D/Line2D) 全部重载。
5. getBounds2D 与对角线端点规范化。
6. 继承 ptSegDist、iterator。
白盒：类形态、superclass=Line2D、implements Serializable、公有 float 字段 x1/y1/x2/y2、3 个构造器。
