# java.awt.geom.RoundRectangle2D.Float 设计案

## 1. API 功能分组
- **公有字段**：`x,y,width,height,arcwidth,archeight`（float）。
- **构造器**：`()`、`(float x,y,w,h,arcw,arch)`。
- **几何**：`getX/getY/getWidth/getHeight`、`getArcWidth/getArcHeight`、`isEmpty()`、`setRoundRect(float…)`、`setRoundRect(double…)`、`setRoundRect(RoundRectangle2D)`、`getBounds2D()`。

## 2. Java 用户如何使用
```
RoundRectangle2D.Float rr = new RoundRectangle2D.Float(0f,0f,20f,20f,10f,10f);
rr.setRoundRect(1f,2f,30f,40f,5f,5f);
Rectangle2D b = rr.getBounds2D();
```

## 3. 开源代码实际用法调研（来源标注）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/geom/RoundRectangle2D.java`：Float 嵌套类。
- JDK `RoundRectIterator.java`：iterator。

## 4. 测试用例设计 + 自动化实现
黑盒（`FloatTest.testBlackBox()`，30–60 断言）：
1. 默认零构造、isEmpty。
2. 6-float 构造、公有字段读取/写入。
3. getX/getY/getWidth/getHeight/getArcWidth/getArcHeight。
4. setRoundRect(float/double/RoundRectangle2D) 重载。
5. getBounds2D、contains 中心/角落。
白盒：类形态、superclass=RoundRectangle2D、implements Serializable、公有 float 字段、2 个构造器。
