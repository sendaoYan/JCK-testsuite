# java.awt.geom.Ellipse2D 设计案

## 1. API 功能分组
- **Shape**：`contains(x,y)`、`intersects`、`contains(x,y,w,h)`、`getPathIterator`、`hashCode`、`equals`。
- **继承（RectangularShape）**：`getBounds2D/getBounds/getFrame/setFrame/contains(Rectangle2D)`。

## 2. Java 用户如何使用
```
Ellipse2D e = new Ellipse2D.Double(0,0,20,20);
boolean inside = e.contains(10,10);    // 中心在内；边界点不含（严格 <）
Rectangle2D b = e.getBounds2D();
```

## 3. 开源代码实际用法调研（来源标注）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/geom/Ellipse2D.java`：contains 使用椭圆方程。
- JDK `EllipseIterator.java`：iterator（MOVETO+4 CUBICTO+CLOSE）。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/Graphics2D.java`：drawOval 注释。

## 4. 测试用例设计 + 自动化实现
黑盒（`Ellipse2DTest.testBlackBox()`，经 Ellipse2D.Double，15–25 断言）：
1. contains 中心（内）、边界点（外，严格 <）、角落（外）。
2. contains 内矩形（内）与整帧矩形（外）。
3. intersects 重叠矩形。
4. getBounds2D。
5. getPathIterator 段计数（1 MOVETO + 4 CUBICTO + 1 CLOSE）。
6. equals/hashCode。
白盒：abstract、extends RectangularShape、contains/intersects/iterator/equals/hashCode 存在。
