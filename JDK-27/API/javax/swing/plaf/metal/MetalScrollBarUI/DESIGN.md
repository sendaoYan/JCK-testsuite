# javax.swing.plaf.metal.MetalScrollBarUI — DESIGN

## 1. API 功能
`MetalScrollBarUI` 是 Metal 观感下 `JScrollBar` 的 UI 委托（`extends BasicScrollBarUI`，非抽象）。方法分组：工厂 `createUI(JComponent)`、生命周期 `installUI/uninstallUI`，以及滑杆轨道/缩略块/按钮的布局与绘制。

## 2. Java 用户如何使用
用户通过 Metal LAF 让 `JScrollBar` 自动挂载本委托。直接使用时：`MetalScrollBarUI ui = new MetalScrollBarUI(); ui.installUI(sb); ui.paint(g, sb); ui.uninstallUI(sb);`。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalScrollBarUI.java:53`：`public class MetalScrollBarUI extends BasicScrollBarUI`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalScrollBarUI.java:103`：`public static ComponentUI createUI(JComponent c)`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java:298`：将 `"ScrollBarUI"` 映射到 `MetalScrollBarUI`。

## 4. 测试用例设计（黑盒部分，`MetalScrollBarUITest.testBlackBox`）
1. `createUI` 返回本类；`installUI(sb)` 无异常。
2. `paint` 画到 `BufferedImage` 无异常；`uninstallUI` 无异常。
