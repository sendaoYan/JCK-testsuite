# javax.swing.border.SoftBevelBorder 设计案

## 1. API 功能
`SoftBevelBorder` 继承 `BevelBorder`，绘制边角柔化的凸起/凹陷斜角。方法分组：

- 构造：`SoftBevelBorder(int bevelType)`；`SoftBevelBorder(int, Color highlight, Color shadow)`；`SoftBevelBorder(int, Color highlightOuter, Color highlightInner, Color shadowOuter, Color shadowInner)`（均委托 `BevelBorder`）。
- 绘制：`paintBorder(...)`（覆盖为柔化画法）。
- 边距：`getBorderInsets(Component, Insets)`（固定 `(3,3,3,3)`）。
- 查询：`isBorderOpaque()`（false）；继承 `BevelBorder` 的 `getBevelType()` 与四色 getter。

## 2. Java 用户如何使用
用户用 `SoftBevelBorder` 获得比 `BevelBorder` 更柔和的立体边界：

```java
SoftBevelBorder soft = new SoftBevelBorder(BevelBorder.RAISED, Color.WHITE, Color.BLACK);
Insets in = soft.getBorderInsets(null);   // (3,3,3,3)
int type = soft.getBevelType();           // BevelBorder.RAISED
```

典型组合链路：`new SoftBevelBorder(type, highlight, shadow)` → `getBevelType()` → 四色 getter → `getBorderInsets()` → `isBorderOpaque()`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/BorderFactory.java`：`sharedSoftRaisedBevel = new SoftBevelBorder(BevelBorder.RAISED);` 与 `public static Border createSoftBevelBorder(int type, Color highlight, Color shadow) { return new SoftBevelBorder(type, highlight, shadow); }`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/BorderFactory.java`）。
- JDK 测试 `test/jdk/java/beans/XMLEncoder/javax_swing_border_SoftBevelBorder.java` 与 `javax_swing_plaf_BorderUIResource_SoftBevelBorderUIResource.java` 使用 `new SoftBevelBorder(...)`。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `BevelBorder`、非 abstract。
- 行为（黑盒）：`testBlackBox()` 覆盖——单参构造类型 round-trip、边距 `(3,3,3,3)`、不透明；两参构造继承 `BevelBorder` 的四色派生；五参构造四色 round-trip；RAISED+LOWERED `paintBorder` smoke。

## 5. 深化追加（第二轮）
- 序列化 round-trip：bevelType 保持、仍非 opaque。
- BorderFactory 数据属性：`createSoftBevelBorder(int)`、`(int,Color,Color)`、`(int,Color,Color,Color,Color)`；工厂 insets 恒 (3,3,3,3)。
