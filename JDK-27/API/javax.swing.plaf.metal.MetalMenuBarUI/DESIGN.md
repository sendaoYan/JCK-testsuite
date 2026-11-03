# javax.swing.plaf.metal.MetalMenuBarUI — DESIGN

## 1. API 功能
`MetalMenuBarUI` 是 Metal 观感下 `JMenuBar` 的 UI 委托（`extends BasicMenuBarUI`，非抽象）。方法分组：工厂 `createUI(JComponent)`、生命周期 `installUI/uninstallUI`、绘制 `update/paint`，以及继承的尺寸方法（`BasicMenuBarUI` 的 `getMinimumSize/getMaximumSize` 返回 `null`）。

## 2. Java 用户如何使用
用户通过 Metal LAF 让 `JMenuBar` 自动挂载本委托。直接使用时：`MetalMenuBarUI ui = new MetalMenuBarUI(); ui.installUI(bar); ui.paint(g, bar); ui.uninstallUI(bar);`。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalMenuBarUI.java:41`：`public class MetalMenuBarUI extends BasicMenuBarUI`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/basic/BasicMenuBarUI.java:227` 与 `:231`：`getMinimumSize/getMaximumSize` 均返回 `null`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java:294`：将 `"MenuBarUI"` 映射到 `MetalMenuBarUI`。

## 4. 测试用例设计（黑盒部分，`MetalMenuBarUITest.testBlackBox`）
1. `createUI` 返回本类；`installUI(bar)` 无异常。
2. `getPreferredSize` 不抛异常；`getMinimumSize/getMaximumSize` 返回 `null`（继承契约）。
3. `update` 与 `paint` 画到 `BufferedImage` 无异常；`uninstallUI` 后菜单数量不变。
