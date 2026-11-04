# java.awtPoint 黑盒行为测试设计

## 1. API 功能
整数二维坐标值类（Point2D.Integer 子类）：translate/setLocation(int|double，double 走 Math.round)/move/getX/getY、distance(distanceSq) 3-4-5 精确、equals 对 Point2D.Double 对称、Serializable(sUID=-5276940640259749850L)、toString=`java.awt.Point[x=..,y=..]`、无 hashCode 重写。

## 2. Java 用户如何使用
```java
new Point(3,4).translate(-1,-1); p.setLocation(-3.6,-4.4) -> (-4,-4)（Math.round）
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 Point.java：setLocation(double,double) 用 Math.round；JLS/Canvas 无关；JDK 自带测试 test/jdk/java/awt/geom 间接使用。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：double 取整语义(-3.6,-4.4)→(-4,-4)；equals(Point2D.Double) 对称；getDeclaredMethod 无 hashCode；3-4-5 距离精确；序列化往返（含极值）；sUID 精确；toString 精确。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。
