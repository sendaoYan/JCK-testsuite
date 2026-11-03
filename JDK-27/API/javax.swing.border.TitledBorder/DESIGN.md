# javax.swing.border.TitledBorder 设计案

## 1. API 功能
`TitledBorder` 在任意边框上叠加一个带标题的字符串，可指定位置/对齐/字体/颜色。方法分组：

- 常量：位置 `DEFAULT_POSITION=0`、`ABOVE_TOP=1`、`TOP=2`、`BELOW_TOP=3`、`ABOVE_BOTTOM=4`、`BOTTOM=5`、`BELOW_BOTTOM=6`；对齐 `DEFAULT_JUSTIFICATION=0`、`LEFT=1`、`CENTER=2`、`RIGHT=3`、`LEADING=4`、`TRAILING=5`。
- 构造：6 个重载，从 `TitledBorder(String)` 到 `TitledBorder(Border, String, int justification, int position, Font, Color)`。
- 绘制/边距：`paintBorder(...)`、`getBorderInsets(Component, Insets)`（把标题所需空间并入边距）、`isBorderOpaque()`（false）。
- 查询：`getTitle()`、`getBorder()`、`getTitlePosition()`、`getTitleJustification()`、`getTitleFont()`、`getTitleColor()`。
- 设置：`setTitle/setBorder/setTitlePosition/setTitleJustification/setTitleFont/setTitleColor`（非法 position/justification 抛 `IllegalArgumentException`）。
- 尺寸/基线：`getMinimumSize(Component)`、`getBaseline(Component, int, int)`、`getBaselineResizeBehavior(Component)`。

## 2. Java 用户如何使用
用户给面板加标题框：

```java
TitledBorder tb = new TitledBorder(new LineBorder(Color.BLACK, 2), "Panel",
                                   TitledBorder.CENTER, TitledBorder.ABOVE_TOP);
String title = tb.getTitle();             // "Panel"
tb.setTitlePosition(TitledBorder.BOTTOM);
Insets in = tb.getBorderInsets(component);
Dimension min = tb.getMinimumSize(component);
```

典型组合链路：`new TitledBorder(border, title, justification, position)` → `getTitle()`/`getBorder()` → `setTitlePosition`/`setTitleJustification` → `getBorderInsets` → `getMinimumSize` → `getBaselineResizeBehavior`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/BorderFactory.java`：`public static TitledBorder createTitledBorder(String title) { return new TitledBorder(title); }` 与 `createTitledBorder(Border, String, int justification, int position, Font, Color)`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/BorderFactory.java`）。
- JDK 测试 `test/jdk/java/beans/XMLEncoder/javax_swing_border_TitledBorder.java`：`return new TitledBorder(new EmptyBorder(1, 2, 3, 4), "TITLE", TitledBorder.CENTER, TitledBorder.ABOVE_TOP, new Font("Arial", Font.ITALIC, 12), Color.RED);`。
- JDK 源码 `javax/swing/plaf/basic/BasicBorders.java` 等 LAF 使用 `TitledBorderUIResource` 渲染组边框标题。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `AbstractBorder`、非 abstract，并校验 6 个位置常量与 6 个对齐常量数值。
- 行为（黑盒）：`testBlackBox()` 覆盖——单参构造 title + 默认 position/justification；全参构造 title/border/justification/position round-trip；空标题时 `getBorderInsets` 等于被包装边框边距；六类 setter round-trip；非法 position/justification 抛 IAE；空标题 `getMinimumSize==(4,4)`、带标题时尺寸增长；`isBorderOpaque()==false`；基线空标题为 -1、负宽抛 IAE、null 组件抛 NPE；`getBaselineResizeBehavior` 随位置返回 `CONSTANT_ASCENT`/`CONSTANT_DESCENT`；空标题 `paintBorder` smoke。
