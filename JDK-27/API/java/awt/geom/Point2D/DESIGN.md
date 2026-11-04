# java.awt.geom.Point2D 设计案

## 1. API 功能分组
- **查询**：`getX()`/`getY()`（抽象）。
- **修改**：`setLocation(double,double)`（抽象）、`setLocation(Point2D)`。
- **距离**：静态 `distance(double,double,double,double)`/`distanceSq(...)`；实例 `distance(double,double)`/`distance(Point2D)`/`distanceSq(double,double)`/`distanceSq(Point2D)`。
- **工具**：`clone()`、`equals/hashCode`。

## 2. Java 用户如何使用
用子类 `Point2D.Double`/`Point2D.Float` 存坐标，`p.setLocation(px, py)` 更新，`p.distance(q)` 求两点距离，`p.distanceSq(q)` 求平方距离（避免开方）。组合：`Point2D v = p.distance(...)` 后用于碰撞检测/最近点判定。

## 3. 开源代码实际用法调研
- `java.awt.geom.AffineTransform.transform(Point2D,Point2D)` 内部 new Point2D.Double/Float 存结果，是 Point2D 子类化的核心消费方（JDK 源码）。
- `java.awt.geom.FlatteningPathIterator`/`Line2D` 等大量用 `Point2D` 作为几何点抽象。
- `test/jdk/java/awt/geom/Point2D/` 无专门用例；但 `java/awt/geom/Line2D/DistanceTest` 用 `Point2D.distance` 断言点线距离。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`Point2DTest.testBlackBox()`，用 Point2D.Double 落地抽象）：
1. 构造 + getX/getY；静态/实例 distance 与 distanceSq（勾股 3-4-5）。
2. setLocation(double,double) 与 setLocation(Point2D)。
3. clone 独立且相等。
4. equals/hashCode 契约（同值等、异值不等、null/非点 false），以及 Double 与 Float 按值跨类相等。
白盒（`testWhiteBox()`）：abstract public class、implements Cloneable、静态与实例 distance/distanceSq、setLocation 重载、clone 签名。
