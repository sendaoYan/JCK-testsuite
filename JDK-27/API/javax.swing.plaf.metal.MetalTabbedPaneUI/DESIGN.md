# javax.swing.plaf.metal.MetalTabbedPaneUI — DESIGN

## 1. API 功能
`MetalTabbedPaneUI` 是 Metal 观感下 `JTabbedPane` 的 UI 委托（`extends BasicTabbedPaneUI`，非抽象、无接口）。方法分组：
- 工厂/构造：`MetalTabbedPaneUI()`、静态 `createUI(JComponent)`。
- 生命周期：继承 `installUI`/`uninstallUI`，并覆写 protected `installDefaults()`。
- 绘制（public）：`paint(Graphics,JComponent)`、`update(Graphics,JComponent)`；另有大量 protected 的 tab 边框/内容边框绘制方法（`paintTabBorder`、`paintTopTabBorder`、`paintContentBorderTopEdge` 等）。

## 2. Java 用户如何使用
用户通过 Metal LAF 让 `JTabbedPane` 自动挂载本委托。直接使用时典型链：
`JTabbedPane tp = new JTabbedPane(); tp.addTab("a", new JLabel()); MetalTabbedPaneUI ui = new MetalTabbedPaneUI(); ui.installUI(tp); ui.update(g,tp); ui.paint(g,tp); ui.uninstallUI(tp);`。`paint` 会按 `tabPane.getTabPlacement()` 绘制 tab 区背景并委托父类绘制内容。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java:303`：将 `"TabbedPaneUI"` 映射到 `MetalTabbedPaneUI`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalTabbedPaneUI.java:896`：`paint(Graphics,JComponent)` 先绘制 tab 区背景再 `super.paint(g,c)`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalTabbedPaneUI.java:813`：`update(Graphics,JComponent)` 覆写绘制入口。

## 4. 测试用例设计（黑盒部分，`MetalTabbedPaneUITest.testBlackBox`）
1. 构造 `MetalTabbedPaneUI` 非空、`createUI` 返回本类实例。
2. `installUI(tp)` 无异常；tabbed pane 宽高为正。
3. 画到 `BufferedImage(100,100,TYPE_INT_ARGB)`：`paint(g,tp)`、`update(g,tp)` 均无异常。
4. `uninstallUI(tp)` 无异常。
