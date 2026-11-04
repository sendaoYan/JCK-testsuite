# java.awt.Rectangle 黑盒行为测试设计

## 1. API 功能
- 构造：`Rectangle()`、`Rectangle(int,int,int,int)`、`Rectangle(int,int)`（w,h）、`Rectangle(Rectangle)`、`Rectangle(Point,Dimension)`、`Rectangle(Point)`、`Rectangle(Dimension)`。
- 公开可变字段：`x`/`y`/`width`/`height`（int）。
- 查询：`getX/getY/getWidth/getHeight`（Rectangle2D 双精度视图）、`getBounds`（返回新对象）、`getBounds2D`、`getLocation`、`getSize`、`isEmpty`、`outcode(double,double)`、`toString`。
- 修改：`setBounds(Rectangle)`、`setBounds(int,int,int,int)`、`setRect(double,double,double,double)`、`reshape`、`setLocation`、`move`、`translate`、`setSize`、`resize`、`grow`、`add(int,int)`、`add(Point)`、`add(Rectangle)`。
- 几何：`contains(Point)`、`contains(int,int)`、`contains(int,int,int,int)`、`contains(Rectangle)`、`inside(int,int)`、`intersects(Rectangle)`、`intersection(Rectangle)`、`union(Rectangle)`、`createIntersection`、`createUnion`。
- 值语义：`equals`、`hashCode`。

## 2. Java 用户如何使用
布局与命中检测最常见载体：组件 `getBounds()`/`setBounds()`，`mouse` 命中用 `contains(x,y)`，滚动视口用 `intersects/intersection`。
```java
Rectangle r = new Rectangle(10, 10, 100, 100);
if (r.contains(e.getX(), e.getY())) { ... }
Rectangle clip = viewport.intersection(damage);
r.translate(dx, dy);
r.grow(2, 2);
```
组合链路：构造 → `setBounds` → `translate`/`grow` → `contains` 命中 → `intersection`/`union` → `equals`/`toString`。

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 `src/java.desktop/share/classes/java/awt/Rectangle.java`：`setRect(double,...)` 用 `clip`+`floor`/`ceil`（原点向下取整、远端向上取整）；`outcode` 用 `OUT_LEFT/TOP/RIGHT/BOTTOM` 位掩码；`intersection` 无交集时返回负尺寸；`grow` 用 long 防溢出。
- JDK 自带测试（真实用法）：`test/jdk/java/awt/Component/Headless/HeadlessPanel.java`（`new Rectangle` + `contains`）、`test/jdk/java/awt/Robot/MouseMoveOffScreen.java`（`new Rectangle` 指定区域）、`test/jdk/java/awt/dnd/DropTargetInInternalFrameTest.java`（`Rectangle.intersects`）。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- 七个构造器 + 默认 0,0,0,0 + 拷贝独立性（`(Point,Dimension)`/`(Point)`/`(Dimension)` 语义）。
- `getX/getY/getWidth/getHeight` 双精度视图；`getBounds` 返回独立副本；`getBounds2D`。
- `setBounds`（值拷贝不别名）/`setRect` 精确整数 + 分数 floor/ceil（10.9,20.1,5,6 → 10,20,6,7）/`reshape`/`move`/`translate`/`setSize`/`resize`/`setLocation`/`getLocation`/`grow`。
- `contains`（点、内矩形、右/下边界排他）/`inside`/`intersects`。
- `intersection`/`union` 精确值；不相交 `intersection` 负尺寸；`createIntersection`/`createUnion` 双精度。
- `add(int,int)`/`add(Point)`/`add(Rectangle)` 扩大包围盒。
- `isEmpty`（0 或负宽高）/`outcode`（内部 0、外部非 0）。
- `equals`/`hashCode` 契约 + `toString` 精确格式。
- 组合链：`setBounds`→`translate`→`grow(-n)` 收缩→`add` 扩大→`contains`。
断言 105 项，全部纯数学精确断言（headless-safe）。


## 5. 深化补充（testContract，JDK-8+JDK-27 双侧对齐）
本任务在保留原有断言基础上追加契约级深化：
testContract：add(Point) 就地扩；贴合矩形交为零宽空矩形；相离为负尺寸；union 空矩形被忽略（观察）；contains/intersects 双精度排他；grow 负收缩；outcode 掩码；setBounds(Rectangle) 拷贝；reshape 别名；序列化；equals 跨实现。
全部为 headless（env -u DISPLAY）实测锁定值，双端 JDK 行为一致。
