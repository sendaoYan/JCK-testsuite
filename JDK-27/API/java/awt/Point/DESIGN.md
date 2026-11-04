# java.awt.Point 黑盒行为测试设计

## 1. API 功能
- 构造：`Point()`（0,0）、`Point(int,int)`、`Point(Point)`。
- 公开字段：`x`、`y`。
- 查询：`getX()/getY()`（Point2D 双精度）、`getLocation()`、`distance/distanceSq`（继承 Point2D，含 Point 参数与双精度重载）、`equals`、`toString`。
- 修改：`setLocation(int,int)`、`setLocation(Point)`、`setLocation(double,double)`（四舍五入）、`move(int,int)`、`translate(int,int)`。

## 2. Java 用户如何使用
最常用的可变坐标值对象：鼠标事件坐标、组件位置 (`getLocation`/`setLocation`)、几何计算。
```java
Point p = new Point(3, 4);
p.translate(10, 20);           // (13,24)
p.setLocation(3.6, 4.4);       // -> (4,4)
double d = p.distance(0, 0);   // 3-4-5 三角
```
组合链路：`new Point` → `move/translate` → `setLocation` → `getLocation` → `equals/distance`。

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/awt/event/`、`test/jdk/java/awt/Focus/` 等用 `MouseEvent.getPoint()` + `translate` 计算相对坐标。
- JDK 源码 `src/java.desktop/share/classes/java/awt/Point.java`：`setLocation(double,double)` 用 `Math.floor(v+0.5)`；`hashCode = x*31+y`（测试只断契约）。
- `test/jdk/java/awt/geom/Point2D/DistanceTests.java`：distance/distanceSq 精确数学断言。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- 三构造器 + 拷贝独立；`getX/getY` 双精度视图；`getLocation` 返回新对象。
- `setLocation` 三个重载（含 double 四舍五入：3.6→4、-3.6→-4，实测）。
- `move`/`translate` 加法语义与回退；组合走位后 equals+distance 精确断言（3-4-5、0 距离）。
- `equals`（相等/交换/null/异类/自反）+ hashCode 契约。
- `toString` 精确格式 `java.awt.Point[x=3,y=4]`。
断言约 32 项，纯数学精确值（headless-safe）。


## 5. 深化补充（testContract，JDK-8+JDK-27 双侧对齐）
本任务在保留原有断言基础上追加契约级深化：
testContract：double 取整语义(-3.6,-4.4)→(-4,-4)；equals(Point2D.Double) 对称；getDeclaredMethod 无 hashCode；3-4-5 距离精确；序列化往返（含极值）；sUID 精确；toString 精确。
全部为 headless（env -u DISPLAY）实测锁定值，双端 JDK 行为一致。
