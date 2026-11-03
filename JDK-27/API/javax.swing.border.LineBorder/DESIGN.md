# javax.swing.border.LineBorder 设计案

## 1. API 功能
`LineBorder` 是单色、任意粗细的线条边框，可选圆角。方法分组：

- 工厂：`static createBlackLineBorder()`、`static createGrayLineBorder()`（缓存单例、粗细 1）。
- 构造：`LineBorder(Color)`（粗细 1、直角）；`LineBorder(Color, int thickness)`；`LineBorder(Color, int thickness, boolean roundedCorners)`。
- 绘制：`paintBorder(...)`。
- 边距：`getBorderInsets(Component, Insets)`（四边均为 `thickness`）。
- 查询：`getLineColor()`、`getThickness()`、`getRoundedCorners()`、`isBorderOpaque()`（`!roundedCorners`）。

## 2. Java 用户如何使用
用户用 `LineBorder` 给组件画简单描边：

```java
LineBorder lb = new LineBorder(Color.RED, 2, true);  // 红、2px、圆角
Insets in = lb.getBorderInsets(null);                 // (2,2,2,2)
boolean rounded = lb.getRoundedCorners();             // true
Border black = LineBorder.createBlackLineBorder();    // 共享的 1px 黑线
```

典型组合链路：`new LineBorder(color, thickness, rounded)` → `getThickness()`/`getLineColor()`/`getRoundedCorners()` → `getBorderInsets()` → `isBorderOpaque()`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/BorderFactory.java`：`public static Border createLineBorder(Color color) { return new LineBorder(color, 1); }` 与 `createLineBorder(color, thickness)`、`createLineBorder(color, thickness, rounded)`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/BorderFactory.java`）。
- JDK 源码 `javax/swing/plaf/basic/BasicBorders.java`：`Border progressBarBorder = new BorderUIResource.LineBorderUIResource(Color.green, 2);`（来源：同目录 `BasicBorders.java`）。
- JDK 测试 `test/jdk/java/beans/XMLEncoder/javax_swing_border_LineBorder.java`：`return new LineBorder(Color.RED, 2, true);`。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `AbstractBorder`、非 abstract。
- 行为（黑盒）：`testBlackBox()` 覆盖——全参构造颜色/粗细/圆角 round-trip、边距=thickness、圆角不透明；单参构造默认粗细 1 直角；两参构造边距 `(4,4,4,4)` 直角不透明；黑白工厂单例缓存且颜色/粗细正确；`paintBorder`（圆角+直角）smoke。
