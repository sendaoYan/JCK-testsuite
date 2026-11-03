# java.awt.geom.Ellipse2D.Float 设计案

## 1. API 功能分组
- **公有字段**：`x,y,width,height`（float）。
- **构造器**：`()`、`(float x,y,w,h)`。
- **几何**：`getX/getY/getWidth/getHeight`、`isEmpty()`、`setFrame(float…)`、`setFrame(double…)`、`getBounds2D()`。

## 2. Java 用户如何使用
```
Ellipse2D.Float e = new Ellipse2D.Float(0f,0f,20f,20f);
e.setFrame(2.5f, 3.5f, 4.5f, 5.5f);
boolean inside = e.contains(10,10);
```

## 3. 开源代码实际用法调研（来源标注）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/geom/Ellipse2D.java`：Float 嵌套类。
- JDK `EllipseIterator.java`：iterator。

## 4. 测试用例设计 + 自动化实现
黑盒（`FloatTest.testBlackBox()`，30–60 断言）：
1. 默认零构造、isEmpty。
2. 4-float 构造、公有字段读取/写入。
3. getX/getY/getWidth/getHeight。
4. setFrame(float/double)。
5. getBounds2D、contains 中心/角落。
白盒：类形态、superclass=Ellipse2D、implements Serializable、公有 float 字段、2 个构造器。
