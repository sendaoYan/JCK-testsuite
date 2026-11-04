# java.awt.geom.RoundRectangle2D.Double 设计案

## 1. API 功能分组
- **公有字段**：`x,y,width,height,arcwidth,archeight`（double）。
- **构造器**：`()`、`(double x,y,w,h,arcw,arch)`。
- **几何**：`getX/getY/getWidth/getHeight`、`getArcWidth/getArcHeight`、`isEmpty()`、`setRoundRect(double…)`、`setRoundRect(RoundRectangle2D)`、`getBounds2D()`。

## 2. Java 用户如何使用
```
RoundRectangle2D.Double rr = new RoundRectangle2D.Double(0,0,20,20,10,10);
if (rr.contains(10,10)) { ... }
```

## 3. 开源代码实际用法调研（来源标注）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/geom/RoundRectangle2D.java`：Double 嵌套类。
- JDK `RoundRectIterator.java`：iterator。

## 4. 测试用例设计 + 自动化实现
黑盒（`DoubleTest.testBlackBox()`，30–60 断言）：
1. 默认零构造、isEmpty。
2. 6-double 构造、公有字段读取/写入。
3. getX/getY/getWidth/getHeight/getArcWidth/getArcHeight。
4. setRoundRect(double/RoundRectangle2D)。
5. getBounds2D、contains 中心/角落。
白盒：类形态、superclass=RoundRectangle2D、implements Serializable、公有 double 字段、2 个构造器。
