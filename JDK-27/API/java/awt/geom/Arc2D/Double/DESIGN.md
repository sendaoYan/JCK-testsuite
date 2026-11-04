# java.awt.geom.Arc2D.Double 设计案

## 1. API 功能分组
- **公有字段**：`x,y,width,height,start,extent`（double）。
- **构造器**：`()`、`(int type)`、`(double x,y,w,h,start,extent,int type)`、`(Rectangle2D,double start,double extent,int type)`。
- **几何**：`getX/getY/getWidth/getHeight`、`isEmpty()`、`setArc(double…)`、`setAngleStart(double)`、`setAngleExtent(double)`、继承 Arc2D 全部行为。

## 2. Java 用户如何使用
```
Arc2D.Double a = new Arc2D.Double(0,0,20,20,0,90,Arc2D.CHORD);
a.setArcByCenter(0,0,10,30,120,Arc2D.PIE);
```

## 3. 开源代码实际用法调研（来源标注）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/geom/Arc2D.java`：Double 嵌套类。
- `~/git/jdk25u-dev/test/jdk/java/awt/geom/Arc2D/Arc2DHitTest.java`：hit test。
- JDK `ArcIterator.java`：iterator。

## 4. 测试用例设计 + 自动化实现
黑盒（`DoubleTest.testBlackBox()`，30–60 断言）：
1. 默认构造（type OPEN、empty）、int-type 构造。
2. 6-double 构造、Rectangle2D 构造。
3. 公有字段读取/写入。
4. getX/getY/getWidth/getHeight、isEmpty。
5. setArc、setAngleStart/Extent、setFrame。
6. 继承 setArcByCenter 帧与起止点、containsAngle。
白盒：类形态、superclass=Arc2D、implements Serializable、公有 double 字段、4 个构造器。
