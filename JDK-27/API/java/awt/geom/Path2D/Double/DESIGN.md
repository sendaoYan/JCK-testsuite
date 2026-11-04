# java.awt.geom.Path2D.Double 设计案

## 1. API 功能分组
- **构造器**：`()`、`(int rule)`、`(int rule, int initialCapacity)`、`(Shape)`、`(Shape, AffineTransform)`。
- **路径构建（double）**：`moveTo/lineTo/quadTo/curveTo`。
- **几何查询**：`getBounds2D()`（返回 Rectangle2D.Double）、`getPathIterator(AT)`、`getPathIterator(AT, flatness)`。
- **其它**：`append(PathIterator, boolean)`、`trimToSize()`、`clone()`。
- **继承**：Path2D 全部公共行为。

## 2. Java 用户如何使用
以 double 精度存储坐标，适合高精度几何：
```
Path2D.Double p = new Path2D.Double();
p.moveTo(0, 0); p.curveTo(10, 20, 30, -20, 40, 0);
PathIterator flat = p.getPathIterator(null, 1.0);  // 扁平化三次曲线
```
默认 winding 为 WIND_NON_ZERO，可用构造器或 setWindingRule 切换。

## 3. 开源代码实际用法调研（来源标注）
- `~/git/jdk25u-dev/test/jdk/java/awt/geom/Path2D/UnitTest.java`：iterator 对拍。
- `~/git/jdk25u-dev/test/jdk/java/awt/geom/Path2D/Path2DCopyConstructor.java`：复制构造。
- `~/git/jdk25u-dev/test/jdk/java/awt/geom/Path2D/EmptyCapacity.java`：容量构造器。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/geom/Path2D.java`：Double 的 doubleCoords 存储与 append(double,double)。

## 4. 测试用例设计 + 自动化实现
黑盒（`DoubleTest.testBlackBox()`，30–60 断言）：
1. 默认 winding + 指定 winding 构造。
2. 完整段链与 iterator 类型/坐标断言（MOVETO/LINETO/QUADTO/CUBICTO/CLOSE）。
3. getBounds2D 精确值。
4. Shape/Shape+AT 构造器。
5. getPathIterator(AT) 变换、扁平化曲线细分。
6. append(PathIterator)、clone 独立性、trimToSize、getCurrentPoint+closePath。
7. 序列化 round-trip：恢复 Path2D.Double 类、winding rule、几何 bounds 与 closePath 后的 currentPoint。
白盒：类形态、superclass=Path2D、implements Serializable、5 个 public 构造器。
