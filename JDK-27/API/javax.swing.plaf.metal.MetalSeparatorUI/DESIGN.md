# javax.swing.plaf.metal.MetalSeparatorUI — DESIGN

## 1. API 功能
`MetalSeparatorUI` 是 Metal 观感下 `JSeparator` 的 UI 委托（`extends BasicSeparatorUI`，非抽象）。方法分组：工厂 `createUI(JComponent)`、绘制 `paint(Graphics, JComponent)`、尺寸 `getPreferredSize(JComponent)`，以及继承的生命周期方法。

## 2. Java 用户如何使用
用户通过 Metal LAF 让 `JSeparator` 自动挂载本委托。直接使用时：`MetalSeparatorUI ui = new MetalSeparatorUI(); ui.installUI(sep); ui.paint(g, sep); ui.uninstallUI(sep);`。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalSeparatorUI.java:53`：`public class MetalSeparatorUI extends BasicSeparatorUI`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalSeparatorUI.java:66`：`public static ComponentUI createUI(JComponent c)`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java:300`：将 `"SeparatorUI"` 映射到 `MetalSeparatorUI`。

## 4. 测试用例设计（黑盒部分，`MetalSeparatorUITest.testBlackBox`）
1. `createUI` 返回本类；`installUI(sep)` 无异常。
2. `paint` 画到 `BufferedImage` 无异常；`getPreferredSize` 返回非空。
3. `uninstallUI` 无异常。
