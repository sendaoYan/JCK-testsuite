# javax.swing.JLayer 测试设计

## 1. API 功能
- 构造器：`JLayer()`（view null）、`JLayer(V view)`、`JLayer(V view, LayerUI<V> ui)`。
- 视图/玻璃面板：`getView/setView`、`getGlassPane/setGlassPane`、`createGlassPane()`。
- UI：`setUI(LayerUI)/getUI`。
- 事件：`setLayerEventMask(long)/getLayerEventMask()`。
- 委托：`setBorder/getBorder` 委托给 view（view 为 JComponent 时）。
- 不支持：`setLayout(非 null)` 抛 `IllegalArgumentException`。

## 2. Java 用户如何使用
典型组合：`new JLayer<JPanel>(panel, new LayerUI<JPanel>())` 装饰已有组件；`setLayerEventMask(AWTEvent.KEY_EVENT_MASK)` 让 LayerUI 接收键盘事件；自定义装饰时 `setView`/`setGlassPane` 组合内容与覆盖层。

## 3. 开源代码实际用法调研
- JDK 自带测试 `test/jdk/javax/swing/JRootPane/RootPaneDecorationSize.java`、`JToolTip/TestTooltipBackgroundColor.java`：在 JLayer 装饰的组件上验证绘制/尺寸。
- JDK 源码 `javax.swing.plaf.LayerUI`：与 JLayer 配合，通过 `processXxxEvent` 拦截事件，说明 layerEventMask 的用途。
- Oracle JLayer 教程示例用 `JLayer` 为组件叠加玻璃面板与动画效果。

## 4. 测试用例设计 + 自动化实现
黑盒覆盖：空/带 view/带 view+ui 三种构造器；`getView/setView` 往返与父子关系、`setView(null)` 移除旧 view；glassPane 往返、createGlassPane 非 null 且不透明；eventMask 往返；`setUI/getUI` 往返；`setLayout(非 null)` 抛 `IllegalArgumentException`、`setLayout(null)` 正常；`setBorder/getBorder` 委托到 view；`updateUI` 可调用。见 `JLayerTest.java` 的 `testBlackBox()`。
