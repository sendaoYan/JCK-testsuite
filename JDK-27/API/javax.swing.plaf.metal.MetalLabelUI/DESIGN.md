# javax.swing.plaf.metal.MetalLabelUI — DESIGN

## 1. API 功能
`MetalLabelUI` 是 Metal 观感下 `JLabel` 的 UI 委托（`extends BasicLabelUI`，非抽象）。方法分组：工厂 `createUI(JComponent)`、protected `paintDisabledText(JLabel, Graphics, String, int, int)` 绘制禁用文本，以及继承的 `paint/update/getPreferredSize` 等。

## 2. Java 用户如何使用
用户通过 Metal LAF 让 `JLabel` 自动挂载本委托。直接使用时：`MetalLabelUI ui = new MetalLabelUI(); ui.installUI(label); ui.paint(g, label); ui.uninstallUI(label);`。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLabelUI.java:47`：`public class MetalLabelUI extends BasicLabelUI`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLabelUI.java:79`：`protected void paintDisabledText(JLabel l, Graphics g, String s, int textX, int textY)`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java:293`：将 `"LabelUI"` 映射到 `MetalLabelUI`。

## 4. 测试用例设计（黑盒部分，`MetalLabelUITest.testBlackBox`）
1. `createUI` 返回本类；`installUI(label)` 无异常。
2. `paint` 与 `update` 画到 `BufferedImage` 无异常。
3. `uninstallUI(label)` 无异常。
