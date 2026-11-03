# java.awt.geom.Ellipse2D.Double 设计案

## 1. API 功能分组
- **公有字段**：`x,y,width,height`（double）。
- **构造器**：`()`、`(double x,y,w,h)`。
- **几何**：`getX/getY/getWidth/getHeight`、`isEmpty()`、`setFrame(double…)`、`getBounds2D()`。

## 2. Java 用户如何使用
```
Ellipse2D.Double e = new Ellipse2D.Double(0,0,20,20);
boolean inside = e.contains(10,10);
```

## 3. 开源代码实际用法调研（来源标注）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/geom/Ellipse2D.java`：Double 嵌套类。
- JDK `EllipseIterator.java`：iterator。

## 4. 测试用例设计 + 自动化实现
黑盒（`DoubleTest.testBlackBox()`，30–60 断言）：
1. 默认零构造、isEmpty。
2. 4-double 构造、公有字段读取/写入。
3. getX/getY/getWidth/getHeight。
4. setFrame。
5. getBounds2D、contains 中心/角落。
白盒：类形态、superclass=Ellipse2D、implements Serializable、公有 double 字段、2 个构造器。
