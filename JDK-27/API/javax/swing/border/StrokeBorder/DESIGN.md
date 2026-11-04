# javax.swing.border.StrokeBorder 设计案

## 1. API 功能
`StrokeBorder` 用任意 `BasicStroke` 描边，边距按线宽向上取整。方法分组：

- 构造：`StrokeBorder(BasicStroke stroke)`（paint 为 null，用组件前景色）；`StrokeBorder(BasicStroke stroke, Paint paint)`（stroke 为 null 抛 NPE）。
- 绘制：`paintBorder(...)`（线宽 > 0 时按 stroke/paint 描矩形）。
- 边距：`getBorderInsets(Component, Insets)`（四边均为 `(int) Math.ceil(stroke.getLineWidth())`）。
- 查询：`getStroke()`、`getPaint()`（未设置时返回 null）。

## 2. Java 用户如何使用
用户用自定义描边绘制边框：

```java
StrokeBorder sb = new StrokeBorder(new BasicStroke(2.5f));
Insets in = sb.getBorderInsets(null);   // (3,3,3,3) = ceil(2.5)
StrokeBorder painted = new StrokeBorder(new BasicStroke(2.0f), Color.RED);
Paint p = painted.getPaint();           // Color.RED
```

典型组合链路：`new StrokeBorder(stroke, paint)` → `getStroke()`/`getPaint()` → `getBorderInsets()` → `paintBorder()`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/BorderFactory.java`：`public static Border createStrokeBorder(BasicStroke stroke) { return new StrokeBorder(stroke); }` 与 `createStrokeBorder(BasicStroke, Paint)`，内部还使用 `createStrokeBorder(new BasicStroke(thickness, cap, join, thickness * 2.0f, array, 0.0f), paint)` 构造虚线边框（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/BorderFactory.java`）。
- JDK 测试 `test/jdk/java/beans/XMLEncoder/javax_swing_border_StrokeBorder.java`：`return new StrokeBorder(new BasicStroke(0), Color.WHITE);`。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `AbstractBorder`、非 abstract。
- 行为（黑盒）：`testBlackBox()` 覆盖——单参构造 stroke round-trip、paint 为 null、边距 `ceil(2.5)==3`；两参构造 paint/stroke round-trip、边距 `ceil(2.0)==2`；零线宽边距 0；null stroke 抛 NPE；`paintBorder` smoke。
