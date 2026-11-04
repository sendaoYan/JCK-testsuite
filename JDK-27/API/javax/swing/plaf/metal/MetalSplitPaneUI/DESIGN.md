# javax.swing.plaf.metal.MetalSplitPaneUI — DESIGN

## 1. API 功能
`MetalSplitPaneUI` 是 Metal 观感下 `JSplitPane` 的 UI 委托（`extends BasicSplitPaneUI`，非抽象、无接口）。方法分组：
- 工厂/构造：`MetalSplitPaneUI()`、静态 `createUI(JComponent)`。
- 分隔条：`createDefaultDivider()` 返回 `new MetalSplitPaneDivider(this)`（类型为 `BasicSplitPaneDivider` 子类）。
- 继承的生命周期/绘制来自 `BasicSplitPaneUI`/`ComponentUI`（`installUI`、`uninstallUI`、`paint` 等）。

## 2. Java 用户如何使用
用户通过 Metal LAF 让 `JSplitPane` 自动挂载本委托。直接使用时典型链：
`MetalSplitPaneUI ui = new MetalSplitPaneUI(); ui.installUI(sp); BasicSplitPaneDivider d = ui.createDefaultDivider(); ...; ui.uninstallUI(sp);`。`createDefaultDivider()` 依赖 `installUI` 后 `BasicSplitPaneUI.splitPane` 字段已设置，因此应先 install 再调用。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java:302`：将 `"SplitPaneUI"` 映射到 `MetalSplitPaneUI`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalSplitPaneUI.java:68`：`createDefaultDivider()` 返回 `new MetalSplitPaneDivider(this)`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/basic/BasicSplitPaneUI.java:320`：`installUI` 中 `splitPane = (JSplitPane) c;`，为 `createDefaultDivider` 提供上下文。

## 4. 测试用例设计（黑盒部分，`MetalSplitPaneUITest.testBlackBox`）
1. 构造 `MetalSplitPaneUI` 非空、`createUI` 返回本类实例。
2. `installUI(sp)` 无异常；split pane 宽高为正。
3. `createDefaultDivider()` 返回非空且 `instanceof BasicSplitPaneDivider`。
4. 画到 `BufferedImage(100,100,TYPE_INT_ARGB)`：`paint(g,sp)` 无异常。
5. `uninstallUI(sp)` 无异常。
