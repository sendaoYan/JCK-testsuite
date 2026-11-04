# javax.swing.border.CompoundBorder 设计案

## 1. API 功能
`CompoundBorder` 把两个边框组合成一个：`outsideBorder` 在外、`insideBorder` 嵌套在外部边框边距之内。方法分组：

- 构造：`CompoundBorder()`（内外均为 null）；`CompoundBorder(Border outside, Border inside)`（任意可为 null）。
- 绘制：`paintBorder(...)`（先画 outside，再按 outside 边距平移后画 inside）。
- 边距：`getBorderInsets(Component, Insets)`（outside 与 inside 边距逐边相加）。
- 查询：`getOutsideBorder()`、`getInsideBorder()`、`isBorderOpaque()`（内外各自为 null 或 opaque 时才为 true）。

## 2. Java 用户如何使用
用户用它叠加“装饰边框 + 留白边框”：

```java
Border decor = new LineBorder(Color.RED, 1);
Border margin = new EmptyBorder(5, 5, 5, 5);
CompoundBorder cb = new CompoundBorder(decor, margin);
Insets in = cb.getBorderInsets(null);   // (6,6,6,6) = 1 + 5
```

典型组合链路：`new CompoundBorder(outside, inside)` → `getOutsideBorder()`/`getInsideBorder()` 校验 → `getBorderInsets()` 校验边距相加 → `isBorderOpaque()` 校验合成不透明性。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/BorderFactory.java`：`public static CompoundBorder createCompoundBorder(Border outsideBorder, Border insideBorder) { return new CompoundBorder(outsideBorder, insideBorder); }`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/BorderFactory.java`）。
- JDK 源码 `javax/swing/plaf/basic/BasicBorders.java`：`Border internalFrameBorder = new BorderUIResource.CompoundBorderUIResource(new BevelBorder(BevelBorder.RAISED, ...), BorderFactory.createLineBorder(...));`（来源：同目录 `BasicBorders.java`）。
- JDK 源码 `javax/swing/plaf/basic/BasicToolBarUI.java`：`return new CompoundBorder(new BasicBorders.RolloverButtonBorder(...), ...);`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/basic/BasicToolBarUI.java`）。
- JDK 测试 `test/jdk/java/beans/XMLEncoder/javax_swing_border_CompoundBorder.java`：`return new CompoundBorder(null, new CompoundBorder());`。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `AbstractBorder`、非 abstract、无接口。
- 行为（黑盒）：`testBlackBox()` 覆盖——默认构造内外为 null、边距 0、null 计为 opaque；两个 `EmptyBorder` 边距相加 `(11,22,33,44)` 且不透明；两个 `LineBorder` 边距相加 `(5,5,5,5)` 且 opaque；null inside 视为 opaque；`paintBorder` 合成 smoke。

## 5. 深化追加（第二轮）
- 序列化 round-trip：内外边框类型与数据保持（LineBorder 厚度 2/红色）、复合 insets=(2+1,2+2,2+3,2+4)。
- BorderFactory 数据属性：`createCompoundBorder(out,in)` insets 相加；`createCompoundBorder()` 零 insets。
