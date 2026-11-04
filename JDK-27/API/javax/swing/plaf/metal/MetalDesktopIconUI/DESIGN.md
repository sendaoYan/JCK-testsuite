# javax.swing.plaf.metal.MetalDesktopIconUI — DESIGN

## 1. API 功能
`MetalDesktopIconUI` 是 Metal 观感下 `JInternalFrame.JDesktopIcon`（内部框架最小化图标）的 UI 委托（`extends BasicDesktopIconUI`，非抽象）。方法分组：工厂 `createUI(JComponent)`、生命周期 `installUI/uninstallUI`、以及继承的 `getMinimumSize/getPreferredSize/getMaximumSize`。

## 2. Java 用户如何使用
用户通过 Metal LAF 让 `JDesktopIcon` 自动挂载本委托。直接使用时：`MetalDesktopIconUI ui = new MetalDesktopIconUI(); ui.installUI(icon); Dimension d = ui.getPreferredSize(icon); ui.uninstallUI(icon);`。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalDesktopIconUI.java:43`：`public class MetalDesktopIconUI extends BasicDesktopIconUI`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java:290`：将 `"DesktopIconUI"` 映射到 `MetalDesktopIconUI`。
- `~/git/jdk25u-dev/test/jdk/javax/swing/plaf/metal/MetalLookAndFeel/Test8039750.java:38`：`new MetalLookAndFeel().getDefaults()` 覆盖含 `DesktopIconUI` 的默认值表。

## 4. 测试用例设计（黑盒部分，`MetalDesktopIconUITest.testBlackBox`）
1. `createUI(icon)` 返回 `MetalDesktopIconUI` 实例。
2. `installUI(icon)` 无异常；`getPreferredSize/getMinimumSize` 不抛异常且尺寸非负。
3. 画到 `BufferedImage`（`paint`）无异常。
4. `uninstallUI(icon)` 无异常。
