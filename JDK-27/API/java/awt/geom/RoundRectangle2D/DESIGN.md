# java.awt.geom.RoundRectangle2D 设计案

## 1. API 功能分组
- **圆角**：`getArcWidth()/getArcHeight()`（抽象）。
- **设置**：`setRoundRect(double…)`（抽象）、`setRoundRect(RoundRectangle2D)`、`setFrame(double…)`。
- **Shape**：`contains(x,y)`、`intersects`、`contains(x,y,w,h)`、`getPathIterator`、`hashCode`、`equals`。
- **继承（RectangularShape）**：`getBounds2D/getBounds/getFrame/contains(Rectangle2D)` 等。

## 2. Java 用户如何使用
```
RoundRectangle2D rr = new RoundRectangle2D.Double(0,0,20,20,10,10);
boolean inside = rr.contains(10,10);   // 中心在内，圆角角落在外
Rectangle2D b = rr.getBounds2D();
```
arc width/height 是整圆角椭圆宽高，不是半径；值为宽高的一半才得到完整圆角。

## 3. 开源代码实际用法调研（来源标注）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/geom/RoundRectangle2D.java`：classify/contains 实现。
- JDK `RoundRectIterator.java`：iterator（MOVETO+4 LINETO+4 CUBICTO+CLOSE）。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/Graphics2D.java`：drawRoundRect 注释引用。

## 4. 测试用例设计 + 自动化实现
黑盒（`RoundRectangle2DTest.testBlackBox()`，经 RoundRectangle2D.Double，15–25 断言）：
1. getArcWidth/getArcHeight。
2. contains 中心/边中点（内）与圆角角落（外）；contains 内矩形与跨角矩形。
3. intersects 重叠矩形与仅触角矩形。
4. getBounds2D。
5. getPathIterator 段计数（1 MOVETO + 4 LINETO + 4 CUBICTO + 1 CLOSE）。
6. setRoundRect(double/RoundRectangle2D)、setFrame 保留圆角。
7. equals/hashCode。
白盒：abstract、extends RectangularShape、抽象 arc 访问器、setRoundRect/contains/intersects/iterator 存在。
