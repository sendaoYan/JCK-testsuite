# javax.swing.plaf.metal.MetalTreeUI — DESIGN

## 1. API 功能
`MetalTreeUI` 是 Metal 观感下 `JTree` 的 UI 委托（`extends BasicTreeUI`，非抽象、无接口）。方法分组：
- 工厂/构造：`MetalTreeUI()`、静态 `createUI(JComponent)`。
- 生命周期：`installUI(JComponent)`、`uninstallUI(JComponent)`；解析 `JTree.lineStyle` 客户端属性（Angled/Horizontal/None）。
- 绘制（public）：`paint(Graphics,JComponent)` 在水平线样式下额外绘制水平分隔线；另有 protected `paintHorizontalSeparators`、`paintVerticalPartOfLeg`、`paintHorizontalPartOfLeg`、`isLocationInExpandControl` 等。

## 2. Java 用户如何使用
用户通过 Metal LAF 让 `JTree` 自动挂载本委托。直接使用时典型链：
`MetalTreeUI ui = new MetalTreeUI(); ui.installUI(tree); ui.paint(g,tree); ui.uninstallUI(tree);`。`installUI` 读取 `tree.getClientProperty("JTree.lineStyle")` 决定连线样式（默认 `LEG_LINE_STYLE`），`paint` 先 `super.paint` 再按样式补画水平分隔线。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java:308`：将 `"TreeUI"` 映射到 `MetalTreeUI`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalTreeUI.java:120`：`installUI` 中读取并解码 `JTree.lineStyle`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalTreeUI.java:189`：`paint(Graphics,JComponent)` 先 `super.paint` 再按 `HORIZ_LINE_STYLE` 补画。

## 4. 测试用例设计（黑盒部分，`MetalTreeUITest.testBlackBox`）
1. 构造 `MetalTreeUI` 非空、`createUI` 返回本类实例。
2. `installUI(tree)` 无异常；tree 宽高为正。
3. 画到 `BufferedImage(100,100,TYPE_INT_ARGB)`：`paint(g,tree)` 无异常。
4. `uninstallUI(tree)` 无异常。
