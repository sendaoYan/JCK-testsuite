# javax.swing.plaf.metal.MetalToggleButtonUI — DESIGN

## 1. API 功能
`MetalToggleButtonUI` 是 Metal 观感下 `JToggleButton` 的 UI 委托（`extends BasicToggleButtonUI`，非抽象、无接口）。方法分组：
- 工厂/构造：`MetalToggleButtonUI()`、静态 `createUI(JComponent)`。
- 默认值：`installDefaults(AbstractButton)`（public）设置 focus/select/disabled 颜色等；`uninstallDefaults`（protected）。
- 绘制（public）：`update(Graphics,JComponent)` 在满足渐变条件时先 `drawGradient` 再 `paint`，否则走父类；另有 protected `paintButtonPressed`、`paintText`、`paintFocus`、`paintIcon`。

## 2. Java 用户如何使用
用户通过 Metal LAF 让 `JToggleButton` 自动挂载本委托。直接使用时典型链：
`MetalToggleButtonUI ui = new MetalToggleButtonUI(); ui.installUI(b); ui.installDefaults(b); ui.update(g,b); ui.paint(g,b); ui.uninstallUI(b);`。`installDefaults` 可单独重复调用以刷新 Metal 默认值。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java:305`：将 `"ToggleButtonUI"` 映射到 `MetalToggleButtonUI`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalToggleButtonUI.java:108`：`installDefaults(AbstractButton)` 设置 Metal 颜色/边框默认值。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalToggleButtonUI.java:169`：`update(Graphics,JComponent)` 处理渐变绘制。

## 4. 测试用例设计（黑盒部分，`MetalToggleButtonUITest.testBlackBox`）
1. 构造 `MetalToggleButtonUI` 非空、`createUI` 返回本类实例。
2. `installUI(b)` 无异常；按钮宽高为正。
3. `installDefaults(b)` 无异常。
4. 画到 `BufferedImage(100,100,TYPE_INT_ARGB)`：`update(g,b)`、`paint(g,b)` 均无异常。
5. `uninstallUI(b)` 无异常。
