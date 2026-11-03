# java.awt.geom.GeneralPath 设计案

## 1. API 功能分组
- **构造器**：`()`、`(int rule)`、`(int rule, int initialCapacity)`、`(Shape)`。
- **继承（自 Path2D.Float / Path2D）**：`moveTo/lineTo/quadTo/curveTo/closePath`、`append`、`getCurrentPoint`、`getWindingRule/setWindingRule`、`getBounds2D`、`getPathIterator`、`contains/intersects`、`transform`、`clone`、`trimToSize`、`reset`。
- 类型为 final 类，extends Path2D.Float。

## 2. Java 用户如何使用
GeneralPath 是 Path2D.Float 的历史别名/子类，用于 legacy 代码：
```
GeneralPath gp = new GeneralPath();
gp.moveTo(0, 0); gp.lineTo(10, 0); gp.lineTo(10, 10); gp.closePath();
if (gp.contains(5, 5)) { ... }
```
可用 Shape 构造器从任意形状复制，也可指定 winding 与初始容量。

## 3. 开源代码实际用法调研（来源标注）
- `~/git/jdk25u-dev/test/jdk/java/awt/geom/GeneralPath/GeneralPathExceptions.java`：非法 setWindingRule/append 异常。
- `~/git/jdk25u-dev/test/jdk/java/awt/geom/GeneralPath/CreateTxReturnsGP.java`：createTransformedShape 返回 GeneralPath。
- `~/git/jdk25u-dev/test/jdk/java/awt/geom/GeneralPath/ContainsPoint.java`：contains 命中。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/geom/GeneralPath.java`：定义为 `final class GeneralPath extends Path2D.Float`。

## 4. 测试用例设计 + 自动化实现
黑盒（`GeneralPathTest.testBlackBox()`，30–60 断言）：
1. 默认/指定 winding、容量构造器。
2. Shape 构造器复制几何。
3. moveTo→lineTo→quadTo→curveTo→closePath 链与 iterator 类型。
4. contains/intersects 方形。
5. getBounds2D、getCurrentPoint 生命周期。
6. append(Shape)、transform、clone 独立性、trimToSize、reset。
白盒：final 类、extends Path2D.Float、is-a Path2D、implements Serializable、4 个 public 构造器。
