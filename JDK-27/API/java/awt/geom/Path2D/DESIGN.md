# java.awt.geom.Path2D 设计案

## 1. API 功能分组
- **常量**：`WIND_EVEN_ODD=0`、`WIND_NON_ZERO=1`（填充规则）。
- **抽象路径构建**：`moveTo/lineTo/quadTo/curveTo(double…)`（抽象）、`closePath()`（final）、`append(Shape, boolean)`、`append(PathIterator, boolean)`（抽象）。
- **状态查询/重置**：`getWindingRule()/setWindingRule(int)`、`getCurrentPoint()`、`reset()`。
- **Shape 协议**：`contains/intersects`（实例 + 静态 PathIterator 重载）、`getBounds()/getBounds2D()`、`getPathIterator(AT)`、`getPathIterator(AT, flatness)`、`createTransformedShape(AT)`。
- **其它**：`transform(AT)`（抽象）、`trimToSize()`（抽象）、`clone()`（抽象）。

## 2. Java 用户如何使用
用户不直接 new Path2D（抽象），而是用 `Path2D.Float/Double`；但通常以 `Path2D` 为变量类型统一处理两条实现链：
```
Path2D p = new Path2D.Double();
p.moveTo(0, 0); p.lineTo(10, 0); p.quadTo(15, 5, 10, 10); p.closePath();
boolean hit = p.contains(5, 3);
Rectangle2D b = p.getBounds2D();
PathIterator it = p.getPathIterator(null, 0.5);   // flatten 曲线
```
填充规则：`p.setWindingRule(Path2D.WIND_EVEN_ODD)` 控制自交路径的空洞判定。

## 3. 开源代码实际用法调研（来源标注）
- `~/git/jdk25u-dev/test/jdk/java/awt/geom/Path2D/UnitTest.java`：遍历 PathIterator 断言段类型/坐标（本用例 iterator 断言同源）。
- `~/git/jdk25u-dev/test/jdk/java/awt/geom/Path2D/Path2DCopyConstructor.java`：`new Path2D.Double(Shape)` 复制构造。
- `~/git/jdk25u-dev/test/jdk/java/awt/geom/Path2D/GetBounds2DPrecisionTest.java`：getBounds2D 精度。
- `~/git/jdk25u-dev/test/jdk/java/awt/geom/Path2D/Path2DExceptions.java`：setWindingRule 非法值抛 IllegalArgumentException。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/geom/GeneralPath.java`：Path2D.Float 的 JDK 内部子类，说明实际继承用法。

## 4. 测试用例设计 + 自动化实现
黑盒（`Path2DTest.testBlackBox()`，经 Path2D.Double 实例，15–25 断言）：
1. 常量值：WIND_EVEN_ODD==0、WIND_NON_ZERO==1。
2. 边规则：同向内正方形，NON_ZERO 填充、EVEN_ODD 挖洞；反向则都挖洞；非法 setWindingRule 抛 IAE。
3. currentPoint 生命周期：moveTo→lineTo→closePath 回到子路径起点。
4. 边界：getBounds2D/getBounds（整型）。
5. reset 清空 currentPoint 与几何。
6. contains/intersects 点与矩形 + 静态 `contains(PathIterator,x,y)`。
7. createTransformedShape 平移。
8. append(Shape) 附加几何并产生可迭代段。
9. append(PathIterator, connect) 语义：connect=true 用一条 LINETO 连接当前点到新子路径起点（全程 1 个 MOVETO、3 条 LINETO）；connect=false 开独立子路径（2 个 MOVETO）。
10. Path2D.Float 同一 NON_ZERO 方形 contains 中心（跨实现一致性）。
白盒：抽象修饰符、implements Shape/Cloneable、抽象方法集、final closePath、常量。
