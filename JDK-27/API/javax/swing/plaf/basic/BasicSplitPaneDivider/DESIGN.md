# javax.swing.plaf.basic.BasicSplitPaneDivider — DESIGN

## 1. API 功能
分割条容器：`BasicSplitPaneDivider(BasicSplitPaneUI)` 构造、`get/setBasicSplitPaneUI`、`set/getDividerSize`、`isMouseOver`、`getPreferredSize`/`getMinimumSize`/`getInsets`。

## 2. Java 用户如何使用
由 `BasicSplitPaneUI.createDefaultDivider()` 创建；用户通过 `JSplitPane` 交互间接使用。测试中先 `installUI(JSplitPane)` 再构造 divider，走 setter/getter 往返。

## 3. 开源代码实际用法调研（来源：JDK 源码 jdk25u-dev + test/jdk）
来源：JDK 源码 `src/java.desktop/share/classes/javax/swing/plaf/basic/BasicSplitPaneDivider.java`（jdk25u-dev）；由 `BasicSplitPaneUI` 组装，`test/jdk` 分割面板回归用例覆盖 divider 尺寸与拖拽。

## 4. 测试用例设计 + 自动化实现（`BasicSplitPaneDividerTest.testBlackBox`）
installUI 后构造 divider → `getBasicSplitPaneUI` 往返 → `setDividerSize/getDividerSize` → `isMouseOver` 初始 false → 尺寸/insets 非空。
