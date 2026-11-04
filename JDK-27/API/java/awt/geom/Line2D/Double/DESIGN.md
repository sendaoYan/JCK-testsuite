# java.awt.geom.Line2D.Double 设计案

## 1. API 功能分组
- **公有字段**：`x1,y1,x2,y2`（double）。
- **构造器**：`()`、`(double x1,y1,x2,y2)`、`(Point2D,Point2D)`。
- **坐标访问**：`getX1/getY1/getP1/getX2/getY2/getP2`。
- **设置**：`setLine(double…)`、继承 `setLine(Point2D,Point2D)`、`setLine(Line2D)`。
- **几何**：`getBounds2D()`；继承 Line2D 全部行为。

## 2. Java 用户如何使用
```
Line2D.Double l = new Line2D.Double(0, 0, 10, 0);
double d = l.ptLineDist(5, 3);
if (l.intersectsLine(5, -5, 5, 5)) { ... }
```
高精度线段，字段公开可直接读写。

## 3. 开源代码实际用法调研（来源标注）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/geom/Line2D.java`：Double 嵌套类。
- `~/git/jdk25u-dev/test/jdk/java/awt/geom/Line2D/NegLineDistSqBug.java`：距离平方非负。
- JDK `LineIterator.java`：Line2D.Double 的 iterator 表示。

## 4. 测试用例设计 + 自动化实现
黑盒（`DoubleTest.testBlackBox()`，30–60 断言）：
1. 默认零构造、4-double 构造、Point2D 构造。
2. 公有字段读取/写入。
3. getX1..getY2/getP1/getP2。
4. setLine(double/Point2D,Point2D/Line2D)。
5. getBounds2D 与对角线端点规范化。
6. 继承 ptSegDist、iterator。
白盒：类形态、superclass=Line2D、implements Serializable、公有 double 字段、3 个构造器。
