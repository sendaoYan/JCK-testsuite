# javax.swing.border.EtchedBorder 设计案

## 1. API 功能
`EtchedBorder` 实现凹陷/凸起的蚀刻边框，颜色可由组件背景动态推导。方法分组：

- 常量：`RAISED = 0`、`LOWERED = 1`。
- 构造：`EtchedBorder()`（LOWERED、颜色 null）；`EtchedBorder(int etchType)`；`EtchedBorder(Color highlight, Color shadow)`（LOWERED）；`EtchedBorder(int etchType, Color highlight, Color shadow)`。
- 绘制：`paintBorder(...)`。
- 边距：`getBorderInsets(Component, Insets)`（固定 `(2,2,2,2)`）。
- 查询：`isBorderOpaque()`（true）；`getEtchType()`；`getHighlightColor()`/`getShadowColor()`（返回指定色或 null）；`getHighlightColor(Component)`/`getShadowColor(Component)`（null 时用 `c.getBackground().brighter()/darker()` 推导）。

## 2. Java 用户如何使用
用户用 `EtchedBorder` 表现表单字段的立体边界，颜色未指定时跟随组件背景：

```java
EtchedBorder eb = new EtchedBorder();                    // 默认 LOWERED
Insets in = eb.getBorderInsets(null);                    // (2,2,2,2)
JLabel label = new JLabel();
label.setBackground(Color.WHITE);
Color hi = eb.getHighlightColor(label);                  // WHITE.brighter()
```

典型组合链路：`new EtchedBorder(type, highlight, shadow)` → `getEtchType()` → `getBorderInsets()` → `getHighlightColor(component)`/`getShadowColor(component)` → `isBorderOpaque()`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/BorderFactory.java`：`static final Border sharedEtchedBorder = new EtchedBorder();` 与 `public static Border createEtchedBorder(Color highlight, Color shadow) { return new EtchedBorder(highlight, shadow); }`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/BorderFactory.java`）。
- JDK 测试 `test/jdk/java/beans/XMLEncoder/javax_swing_border_EtchedBorder.java` 与 `javax_swing_plaf_BorderUIResource_EtchedBorderUIResource.java` 使用 `new EtchedBorder(...)` 进行编码测试。
- JDK 源码 `javax/swing/plaf/basic/BasicBorders.java` 等 LAF 内部大量使用 `EtchedBorder`/`EtchedBorderUIResource` 绘制分隔线。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `AbstractBorder`、常量 `RAISED==0`/`LOWERED==1`。
- 行为（黑盒）：`testBlackBox()` 覆盖——默认 LOWERED 且颜色为 null；边距 `(2,2,2,2)`；`isBorderOpaque()==true`；`EtchedBorder(RAISED)` 类型 round-trip；颜色构造默认 LOWERED 且颜色 round-trip；四参构造 round-trip；组件背景推导 `brighter()/darker()`；指定色优先；`paintBorder` smoke。

## 5. 深化追加（第二轮）
- 序列化 round-trip：etchType/highlight/shadow 逐项保持。
- BorderFactory 数据属性：`createEtchedBorder()`（默认 LOWERED）、`(int)`、`(Color,Color)`（默认 LOWERED）、`(int,Color,Color)`；工厂 insets 恒 (2,2,2,2)。
