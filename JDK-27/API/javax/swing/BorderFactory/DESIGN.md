# javax.swing.BorderFactory 设计案

## 1. API 功能
`BorderFactory` 是边框工厂，全是静态方法。方法分组：
- 线条/空边框：`createLineBorder(Color[, int[, boolean]])`、`createEmptyBorder([t,l,b,r])`。
- 3D 边框：`createRaisedBevelBorder/createLoweredBevelBorder`、`createBevelBorder(int[, colors])`、`createSoftBevelBorder`。
- 组合：`createCompoundBorder([outer, inner])`。
- 蚀刻：`createEtchedBorder([type[, highlight, shadow]])`。
- 标题：`createTitledBorder(...)` 多个重载。
- 磨砂：`createMatteBorder(t,l,b,r, Color|Icon)`。
- 描边：`createStrokeBorder(BasicStroke[, Paint])`、`createDashedBorder(Paint[, length, spacing[, rounding, phase]])`。

## 2. Java 用户如何使用
用户用工厂方法快速创建边框并挂到组件上：
```java
panel.setBorder(BorderFactory.createCompoundBorder(
    BorderFactory.createLineBorder(Color.BLACK, 1),
    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
```
典型链路：`createLineBorder` → `createEmptyBorder` → `createCompoundBorder` → `setBorder`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/plaf/basic/BasicLookAndFeel.java` 大量调用 `BorderFactory.create*` 构建默认边框（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/basic/BasicLookAndFeel.java`）。
- JDK 源码 `javax/swing/border/` 各边框类（`LineBorder`、`TitledBorder` 等）是 `BorderFactory` 的产出类型。
- JDK 测试 `test/jdk/javax/swing/border/` 直接验证各边框 insets 与绘制。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `Object`、无接口。
- 行为（黑盒）：`testBlackBox()` 覆盖 `createLineBorder` 的 insets/opaque/颜色、`createEmptyBorder` 四边 insets、`createCompoundBorder` 边距求和、`createMatteBorder` 四边 insets、`createTitledBorder` 标题、蚀刻/斜面/软斜面/描边/虚线边框类型与实例。
