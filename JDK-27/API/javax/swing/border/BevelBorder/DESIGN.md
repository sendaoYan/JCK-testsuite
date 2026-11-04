# javax.swing.border.BevelBorder 设计案

## 1. API 功能
`BevelBorder` 实现双线立体斜角边框（凸起/凹陷），颜色可动态推导。方法分组：

- 常量：`RAISED = 0`、`LOWERED = 1`。
- 构造：`BevelBorder(int bevelType)`；`BevelBorder(int, Color highlight, Color shadow)`（派生四色：`highlightOuter=highlight.brighter()`、`highlightInner=highlight`、`shadowOuter=shadow`、`shadowInner=shadow.brighter()`）；`BevelBorder(int, Color highlightOuter, Color highlightInner, Color shadowOuter, Color shadowInner)`。
- 绘制：`paintBorder(...)`（RAISED/LOWERED 分发到 `paintRaisedBevel`/`paintLoweredBevel`）。
- 边距：`getBorderInsets(Component, Insets)`（固定 `(2,2,2,2)`）。
- 查询：`getBevelType()`、四色 getter（无参返回指定色或 null；带 `Component` 参数在 null 时用 `c.getBackground()` 的 `brighter()/brighter()/darker()/darker()` 推导）、`isBorderOpaque()`（true）。

## 2. Java 用户如何使用
用户用 `BevelBorder` 表现按钮/面板的立体感：

```java
BevelBorder raised = new BevelBorder(BevelBorder.RAISED, Color.WHITE, Color.BLACK);
Insets in = raised.getBorderInsets(null);   // (2,2,2,2)
Color ho = raised.getHighlightOuterColor(); // WHITE.brighter()
```

典型组合链路：`new BevelBorder(type, highlight, shadow)` → `getBevelType()` → 四色 getter → `getBorderInsets()` → `isBorderOpaque()`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/BorderFactory.java`：`static final Border sharedRaisedBevel = new BevelBorder(BevelBorder.RAISED);` 与 `public static Border createBevelBorder(int type, Color highlight, Color shadow) { return new BevelBorder(type, highlight, shadow); }`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/BorderFactory.java`）。
- JDK 源码 `javax/swing/plaf/basic/BasicBorders.java`：`new BevelBorder(BevelBorder.RAISED, ...)` 参与 `CompoundBorderUIResource` 组合（来源：同目录 `BasicBorders.java`）。
- JDK 测试 `test/jdk/java/beans/XMLEncoder/javax_swing_border_BevelBorder.java`：`return new BevelBorder(BevelBorder.RAISED, Color.RED, Color.GREEN, Color.BLUE, Color.WHITE);`。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `AbstractBorder`、常量 `RAISED==0`/`LOWERED==1`。
- 行为（黑盒）：`testBlackBox()` 覆盖——单参构造类型 round-trip、颜色全 null、边距 `(2,2,2,2)`、opaque；两参构造四色派生（`brighter()`/原值/原值/`brighter()`）；五参构造四色 round-trip；组件背景四色推导；指定色优先；RAISED+LOWERED `paintBorder` smoke。
