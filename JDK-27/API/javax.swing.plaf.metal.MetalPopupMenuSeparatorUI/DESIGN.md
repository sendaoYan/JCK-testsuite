# javax.swing.plaf.metal.MetalPopupMenuSeparatorUI — DESIGN

## 1. API 功能
`MetalPopupMenuSeparatorUI` 是 Metal 观感下弹出菜单分隔线的 UI 委托（`extends MetalSeparatorUI`，非抽象）。方法分组：工厂 `createUI(JComponent)`、继承自 `MetalSeparatorUI`/`BasicSeparatorUI` 的尺寸与绘制逻辑。

## 2. Java 用户如何使用
用户通过 Metal LAF 让弹出菜单分隔线自动挂载本委托。直接使用时：`MetalPopupMenuSeparatorUI ui = new MetalPopupMenuSeparatorUI(); ui.installUI(sep); ui.paint(g, sep); ui.uninstallUI(sep);`。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalPopupMenuSeparatorUI.java:44`：`public class MetalPopupMenuSeparatorUI extends MetalSeparatorUI`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalPopupMenuSeparatorUI.java:57`：`public static ComponentUI createUI(JComponent c)`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java:295`：将 `"PopupMenuSeparatorUI"` 映射到 `MetalPopupMenuSeparatorUI`。

## 4. 测试用例设计（黑盒部分，`MetalPopupMenuSeparatorUITest.testBlackBox`）
1. `createUI` 返回本类；`installUI(sep)` 无异常。
2. `paint` 画到 `BufferedImage` 无异常；`uninstallUI` 无异常。
