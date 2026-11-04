# javax.swing.border.AbstractBorder 设计案

## 1. API 功能
`AbstractBorder` 是所有 Swing 边框的抽象基类，实现 `Border` 与 `Serializable`。方法分组：

- 构造：`protected AbstractBorder()`（供子类调用）。
- 绘制：`paintBorder(Component, Graphics, int, int, int, int)`（默认空实现，不绘制）。
- 边距：`getBorderInsets(Component)`（返回全 0 的新 `Insets`）；`getBorderInsets(Component, Insets)`（把传入对象清零并返回同一对象）。
- 查询：`isBorderOpaque()`（默认 `false`）。
- 内部矩形：`getInteriorRectangle(Component, int, int, int, int)`（实例方法，委托静态版）；`static getInteriorRectangle(Component, Border, int, int, int, int)`（用 `x + insets.left`、`y + insets.top`、`width - left - right`、`height - top - bottom` 计算，`Border` 为 null 按 0 边距处理）。
- 基线：`getBaseline(Component, int, int)`（width/height 为负抛 `IllegalArgumentException`，默认返回 -1）；`getBaselineResizeBehavior(Component)`（component 为 null 抛 `NullPointerException`，默认返回 `OTHER`）。

## 2. Java 用户如何使用
用户通常不直接实例化 `AbstractBorder`（抽象类），而是继承它实现自定义边框，或使用其静态 `getInteriorRectangle` 计算去掉边框后的可绘制区域：

```java
AbstractBorder b = new AbstractBorder() {};          // 匿名子类
Insets in = b.getBorderInsets(null);                  // (0,0,0,0)
Rectangle interior = AbstractBorder.getInteriorRectangle(
        null, new EmptyBorder(1, 2, 3, 4), 0, 0, 100, 100); // (2,1,94,96)
```

典型组合链路：`new AbstractBorder(){}` → `getBorderInsets` → `getInteriorRectangle` → `getBaseline` → `getBaselineResizeBehavior`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/border/EmptyBorder.java`、`LineBorder.java`、`CompoundBorder.java` 等全部继承 `AbstractBorder`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/border/`，如 `public class LineBorder extends AbstractBorder`）。
- JDK 源码 `javax/swing/plaf/basic/BasicBorders.java`：`static class RolloverMarginBorder extends EmptyBorder { ... }`（间接继承 `AbstractBorder`）。
- JDK 源码 `javax/swing/JComponent.java` 在 `getBorder`/`setBorder` 后调用 `Border.getBorderInsets` 计算组件布局边距（间接使用该基类行为）。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验 abstract、实现 `Border`+`Serializable`、父类为 `Object`。
- 行为（黑盒）：`testBlackBox()` 用匿名子类覆盖——默认边距全 0、`isBorderOpaque()==false`；`getBorderInsets(c, insets)` 复用同一对象并清零；`null` insets 抛 NPE；`getInteriorRectangle` 零边距不变、`EmptyBorder(1,2,3,4)` 减边距、null 边框按 0 处理；`getBaseline` 默认 -1、负宽高抛 IAE；`getBaselineResizeBehavior` null 抛 NPE、非 null 返回 `OTHER`；`paintBorder` 空实现 smoke。
