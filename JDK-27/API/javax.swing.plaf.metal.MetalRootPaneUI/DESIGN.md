# javax.swing.plaf.metal.MetalRootPaneUI — DESIGN

## 1. API 功能
`MetalRootPaneUI` 是 Metal 观感下 `JRootPane` 的 UI 委托（`extends BasicRootPaneUI`，非抽象）。方法分组：工厂 `createUI(JComponent)`、生命周期 `installUI/uninstallUI`、`propertyChange`，以及内部的 `MetalRootLayout`（负责标题栏/菜单栏/内容面板布局）。本类不重写 `getPreferredSize`。

## 2. Java 用户如何使用
用户通过 Metal LAF 让 `JRootPane` 自动挂载本委托。直接使用时：`MetalRootPaneUI ui = new MetalRootPaneUI(); ui.installUI(rp); ui.paint(g, rp); ui.uninstallUI(rp);`。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalRootPaneUI.java:80`：`public class MetalRootPaneUI extends BasicRootPaneUI`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalRootPaneUI.java:462`：`MetalRootLayout.preferredLayoutSize` 计算标题/菜单/内容面板尺寸。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java:309`：将 `"RootPaneUI"` 映射到 `MetalRootPaneUI`。

## 4. 测试用例设计（黑盒部分，`MetalRootPaneUITest.testBlackBox`）
1. `createUI` 返回本类；`installUI(rp)` 无异常。
2. `getPreferredSize` 不抛异常（继承默认返回 null）；`propertyChange` 未知属性不抛异常。
3. `paint` 画到 `BufferedImage` 无异常；`uninstallUI` 无异常。
