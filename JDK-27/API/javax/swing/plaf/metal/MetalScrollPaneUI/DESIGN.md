# javax.swing.plaf.metal.MetalScrollPaneUI — DESIGN

## 1. API 功能
`MetalScrollPaneUI` 是 Metal 观感下 `JScrollPane` 的 UI 委托（`extends BasicScrollPaneUI`，非抽象、无接口）。方法分组：
- 工厂/构造：`MetalScrollPaneUI()`、静态 `createUI(JComponent)`。
- 生命周期：`installUI(JComponent)`、`uninstallUI(JComponent)`。
- 监听器（重载）：`installListeners(JScrollPane)`、`uninstallListeners(JScrollPane)`（public，deprecated）与 `uninstallListeners(JComponent)`（protected）。
- 内部：`updateScrollbarsFreeStanding()` 依据边框是否为 `MetalBorders.ScrollPaneBorder` 设置滚动条 `MetalScrollBarUI.FREE_STANDING_PROP` 客户端属性；`createScrollBarSwapListener()` 返回监听滚动条替换的属性监听器。

## 2. Java 用户如何使用
用户一般不直接 new，而是让 `JScrollPane` 在 Metal LAF 下自动选择本委托（由 `MetalLookAndFeel` 注册，见第 3 节）。直接构造时典型链：
`MetalScrollPaneUI ui = new MetalScrollPaneUI(); ui.installUI(sp); ui.installListeners(sp); ...; ui.uninstallListeners(sp); ui.uninstallUI(sp);`。`installUI` 后 `updateScrollbarsFreeStanding()` 会写滚动条客户端属性；`uninstallUI` 会清空该属性。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java:299`：将 `"ScrollPaneUI"` 映射到 `MetalScrollPaneUI`（Metal LAF 的 UI 类注册表）。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalScrollPaneUI.java:95`：`installListeners(JScrollPane)` 调用父类后注册 `scrollBarSwapListener`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalScrollPaneUI.java:114`：`uninstallListeners(JScrollPane)` 移除 `scrollBarSwapListener`。

## 4. 测试用例设计（黑盒部分，`MetalScrollPaneUITest.testBlackBox`）
1. 构造 `MetalScrollPaneUI` 非空、`createUI` 返回本类实例。
2. `installUI(sp)` 无异常；sp 宽高为正。
3. `installListeners(sp)` / `uninstallListeners(sp)` 两个 public 重载均可调用无异常。
4. 画到 `BufferedImage(100,100,TYPE_INT_ARGB)`：`paint(g,sp)` 无异常。
5. `uninstallUI(sp)` 无异常。
