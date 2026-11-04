# java.awt.geom.Path2D.Float 设计案

## 1. API 功能分组
- **构造器**：`()`、`(int rule)`、`(int rule, int initialCapacity)`、`(Shape)`、`(Shape, AffineTransform)`。
- **路径构建（float + double 重载）**：`moveTo/lineTo/quadTo/curveTo`。
- **几何查询**：`getBounds2D()`（返回 Rectangle2D.Float）、`getPathIterator(AT)`、`getPathIterator(AT, flatness)`。
- **其它**：`append(PathIterator, boolean)`、`clone()`。JDK-9+ 新增 `trimToSize()`（JDK-8 无此方法，不覆盖）。
- **继承**：Path2D 的 closePath/getCurrentPoint/getWindingRule/contains/intersects/createTransformedShape 等。

## 2. Java 用户如何使用
以 float 坐标存储的路径，坐标以 float[] 缓存：
```
Path2D.Float p = new Path2D.Float(Path2D.WIND_EVEN_ODD, 16);
p.moveTo(0, 0); p.lineTo(10, 0); p.quadTo(15, 5, 10, 10); p.closePath();
Rectangle2D b = p.getBounds2D();
PathIterator it = p.getPathIterator(null, 0.5);
```
复制已有 Shape：`new Path2D.Float(shape)` 或带变换 `new Path2D.Float(shape, at)`。

## 3. 开源代码实际用法调研（来源标注）
- `~/git/jdk25u-dev/test/jdk/java/awt/geom/Path2D/Path2DCopyConstructor.java`：Shape 构造器复制路径。
- `~/git/jdk25u-dev/test/jdk/java/awt/geom/Path2D/Path2DGrow.java`：坐标数组扩容。
- `~/git/jdk25u-dev/test/jdk/java/awt/geom/Path2D/UnitTest.java`：iterator 段类型与坐标对拍。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/geom/Path2D.java`：Float 内部 append(float,float) 与 expandCoords。

## 4. 测试用例设计 + 自动化实现
黑盒（`FloatTest.testBlackBox()`，30–60 断言）：
1. 默认/指定 winding rule 构造。
2. 完整链 moveTo→lineTo→quadTo→curveTo→closePath，iterator 逐一断 MOVETO/LINETO/QUADTO/CUBICTO/CLOSE 类型与坐标。
3. float 与 double 重载均可调用。
4. Shape/Shape+AT 构造器复制与变换。
5. getPathIterator(AT) 平移坐标；getPathIterator(AT, flatness) 细分曲线。
6. append(PathIterator)、clone 独立性、getCurrentPoint（JDK-27 侧另测 trimToSize）。
7. 序列化 round-trip：恢复 Path2D.Float 类、winding rule、几何 bounds 与 closePath 后的 currentPoint。
白盒：类形态、superclass=Path2D、implements Serializable、5 个 public 构造器。
