# javax.swing.plaf.metal.MetalTabbedPaneUI.TabbedPaneLayout — DESIGN

## 1. API 功能
`MetalTabbedPaneUI.TabbedPaneLayout` 是 `MetalTabbedPaneUI` 的 public 内部类（`extends BasicTabbedPaneUI.TabbedPaneLayout`）。方法分组：继承的 `addTab/removeTab`、`calculateTabRects/layoutContainer`，以及重写的 protected `normalizeTabRuns`（仅水平方向规范化）、`rotateTabRuns`（空实现不旋转）、`padSelectedTab`（空实现不填充）。

## 2. Java 用户如何使用
用户通过 `MetalTabbedPaneUI` 的布局管理器使用它。直接使用时：`MetalTabbedPaneUI ui = new MetalTabbedPaneUI(); ui.installUI(tp); TabbedPaneLayout l = ui.new TabbedPaneLayout(); l.addTab("t", SwingConstants.TOP, 0); l.layoutContainer(tp);`。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalTabbedPaneUI.java:1305`：`public class TabbedPaneLayout extends BasicTabbedPaneUI.TabbedPaneLayout`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalTabbedPaneUI.java:1314`：`protected void normalizeTabRuns(int tabPlacement, int tabCount, int start, int max)`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java:303`：将 `"TabbedPaneUI"` 映射到 `MetalTabbedPaneUI`。

## 4. 测试用例设计（黑盒部分，`TabbedPaneLayoutTest.testBlackBox`）
1. `installUI(tp)` 后 `ui.new TabbedPaneLayout()` 非空。
2. `addTab("t", SwingConstants.TOP, 0)` 后 `layoutContainer(tp)` 无异常。
3. `minimumLayoutSize/preferredLayoutSize` 返回非空。
