# javax.swing.plaf.metal.MetalToolTipUI — DESIGN

## 1. API 功能
`MetalToolTipUI` 是 Metal 观感下 `JToolTip` 的 UI 委托（`extends BasicToolTipUI`，非抽象、无接口）。方法分组：
- 常量：`public static final int padSpaceBetweenStrings = 12`。
- 工厂/构造：`MetalToolTipUI()`、静态 `createUI(JComponent)`。
- 生命周期：`installUI(JComponent)`（记录 tip、计算小字体与加速键分隔符）、`uninstallUI(JComponent)`。
- 绘制/布局（public）：`paint(Graphics,JComponent)`、`getPreferredSize(JComponent)`。
- 加速键（public）：`getAcceleratorString()`（无参版本依赖 `tip` 字段）；另有 private `getAcceleratorString(JToolTip)` 与 `calcAccelSpacing`、`isAcceleratorHidden`。

## 2. Java 用户如何使用
用户通过 Metal LAF 让 `JToolTip` 自动挂载本委托。直接使用时典型链：
`MetalToolTipUI ui = new MetalToolTipUI(); ui.installUI(tip); Dimension d = ui.getPreferredSize(tip); String s = ui.getAcceleratorString(); ...; ui.uninstallUI(tip);`。无参 `getAcceleratorString()` 在 `tip == null` 或加速键隐藏、或 tip 无 `AbstractButton` 组件时返回 `""`。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java:307`：将 `"ToolTipUI"` 映射到 `MetalToolTipUI`。
- `~/git/jdk25u-dev/test/jdk/javax/swing/JToolTip/bug5047379.java:86-102`：`MetalToolTipUI toolTipObj = (MetalToolTipUI) MetalToolTipUI.createUI(tooltip); toolTipObj.installUI(tooltip); toolTipObj.getAcceleratorString();` 断言非空。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalToolTipUI.java:62`：`public static final int padSpaceBetweenStrings = 12;`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalToolTipUI.java:198`：无参 `getAcceleratorString()` 返回 `""` 的条件分支。

## 4. 测试用例设计（黑盒部分，`MetalToolTipUITest.testBlackBox`）
1. 构造 `MetalToolTipUI` 非空、`createUI` 返回本类实例。
2. `installUI(tip)` 无异常；tool tip 宽高为正。
3. `getPreferredSize(tip)` 非空且宽高为正（tip 设置文本 "hello" 保证有内容）。
4. `getAcceleratorString()` 返回 `""`；`padSpaceBetweenStrings == 12`。
5. 画到 `BufferedImage(100,100,TYPE_INT_ARGB)`：`paint(g,tip)` 无异常；`uninstallUI(tip)` 无异常。
