# javax.swing.plaf.metal.MetalRadioButtonUI — DESIGN

## 1. API 功能
`MetalRadioButtonUI` 是 Metal 观感下 `JRadioButton` 的 UI 委托（`extends BasicRadioButtonUI`，非抽象）。方法分组：工厂 `createUI(JComponent)`、生命周期 `installUI/uninstallUI`，以及继承的选中态/焦点态绘制。

## 2. Java 用户如何使用
用户通过 Metal LAF 让 `JRadioButton` 自动挂载本委托。直接使用时：`MetalRadioButtonUI ui = new MetalRadioButtonUI(); ui.installUI(rb); ui.paint(g, rb); ui.uninstallUI(rb);`。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalRadioButtonUI.java:56`：`public class MetalRadioButtonUI extends BasicRadioButtonUI`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalRadioButtonUI.java:92`：`public static ComponentUI createUI(JComponent c)`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java:297`：将 `"RadioButtonUI"` 映射到 `MetalRadioButtonUI`。

## 4. 测试用例设计（黑盒部分，`MetalRadioButtonUITest.testBlackBox`）
1. `createUI` 返回本类；`installUI(rb)` 无异常。
2. `paint` 画到 `BufferedImage` 无异常；`uninstallUI` 无异常。
