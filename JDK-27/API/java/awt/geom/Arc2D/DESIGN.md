# java.awt.geom.Arc2D 设计案

## 1. API 功能分组
- **常量**：`OPEN=0`、`CHORD=1`、`PIE=2`。
- **角度**：`getAngleStart/getAngleExtent`（抽象）、`setAngleStart(double)/setAngleExtent(double)`（抽象）、`setAngleStart(Point2D)`、`setAngles(double×4)`、`setAngles(Point2D,Point2D)`、`containsAngle(double)`。
- **端点**：`getStartPoint()/getEndPoint()`。
- **设置**：`setArc(double…)`（抽象）、`setArc(Point2D,Dimension2D,…)`、`setArc(Rectangle2D,…)`、`setArc(Arc2D)`、`setArcByCenter(cx,cy,r,start,extent,type)`、`setArcByTangent(Point2D×3,radius)`、`setArcType(int)`、`setFrame`。
- **Shape**：`getBounds2D/getBounds/getPathIterator/contains/intersects/hashCode/equals`。

## 2. Java 用户如何使用
```
Arc2D arc = new Arc2D.Double(0,0,20,20,0,90,Arc2D.PIE);
arc.setArcByCenter(0,0,10, 45, 90, Arc2D.OPEN);   // 圆心+半径方式
Point2D start = arc.getStartPoint();
boolean in = arc.containsAngle(60);
```
角度以度为单位，0° 指向 +x 方向，正角逆时针（y-down 坐标下视觉顺时针），负 extent 反向。

## 3. 开源代码实际用法调研（来源标注）
- `~/git/jdk25u-dev/test/jdk/java/awt/geom/Arc2D/SetAnglesTest.java`：setAngles。
- `~/git/jdk25u-dev/test/jdk/java/awt/geom/Arc2D/ChordContainsTest.java`：CHORD contains。
- `~/git/jdk25u-dev/test/jdk/java/awt/geom/Arc2D/SmallExtAngleTest.java`：小角度。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/geom/Arc2D.java`：setArcByCenter/normalizeDegrees/containsAngle。
- JDK `ArcIterator.java`：iterator。

## 4. 测试用例设计 + 自动化实现
黑盒（`Arc2DTest.testBlackBox()`，经 Arc2D.Double，15–25 断言）：
1. 常量值 OPEN/CHORD/PIE。
2. setArcByCenter 帧/角度/起止点；负 extent 反向终点。
3. PIE/CHORD/OPEN 对中心的 contains 差异；setArcType 改变包含性。
4. containsAngle 内外。
5. setAngles(double/Point2D)、setAngleStart(Point2D)。
6. setArc(Point2D,Dimension2D)/(Rectangle2D)/(Arc2D)。
7. getBounds2D、PIE iterator 段结构、equals/hashCode。
白盒：abstract、extends RectangularShape、常量、抽象/关键方法存在。
