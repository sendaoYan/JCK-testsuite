# javax.swing.JScrollBar 黑盒行为测试设计

## 1. API 功能
JScrollBar 是滚动条控件，继承 JComponent，实现 Adjustable 与 Accessible，内部以 BoundedRangeModel 承载 value/extent/min/max。

- 构造器：`JScrollBar()`（VERTICAL, 0, 10, 0, 100）、`JScrollBar(int orientation)`、`JScrollBar(int orientation, int value, int extent, int min, int max)`。
- 方向：`getOrientation/setOrientation`（非法值抛 IAE）。
- 区间：`getValue/setValue`、`getMinimum/setMinimum`、`getMaximum/setMaximum`、`getVisibleAmount/setVisibleAmount`、`setValues(newValue, newExtent, newMin, newMax)`。
- 增量：`getUnitIncrement()/setUnitIncrement()`、`getBlockIncrement()/setBlockIncrement()`、`getUnitIncrement(int)/getBlockIncrement(int)`（Adjustable 接口）。
- 模型：`getModel/setModel(BoundedRangeModel)`，默认 DefaultBoundedRangeModel。
- 调整事件：`addAdjustmentListener/removeAdjustmentListener/getAdjustmentListeners`；模型变化转换为 AdjustmentEvent(TRACK)。
- 值调整中标志：`getValueIsAdjusting/setValueIsAdjusting`。

## 2. Java 用户如何使用
- `JScrollBar sb = new JScrollBar(JScrollBar.HORIZONTAL, 0, 10, 0, 100);` 自定义区间。
- `sb.setValue(...)` 程序化滚动；`sb.addAdjustmentListener(e -> ...)` 响应滚动。
- `sb.setUnitIncrement(5); sb.setBlockIncrement(50);` 调整步进。
- 通过 `sb.getModel()` 直接操作 BoundedRangeModel。

## 3. 开源代码实际用法调研
JDK 自带测试（`~/git/jdk25u-dev/test/jdk`）：
- `test/jdk/java/awt/ScrollPane/ScrollPaneFlicker.java` 直接使用 JScrollBar。
- `test/jdk/java/awt/Component/PaintGlitchTest/PaintGlitchTest.java` 用 JScrollBar 触发滚动。
- JScrollPane 内部默认用 `createVerticalScrollBar()/createHorizontalScrollBar()` 创建 JScrollBar 并委托 BoundedRangeModel。
以上来源为 `~/git/jdk25u-dev/test/jdk` 与 `src/java.desktop/share/classes/javax/swing/JScrollPane.java`。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`JScrollBarTest.testBlackBox()`）：
1. 默认构造值（VERTICAL / 0 / 0 / 100 / extent 10 / unit 1 / block 10 / DefaultBoundedRangeModel）。
2. 方向构造器与五参构造器的区间/增量语义（block 默认等于 extent）。
3. setOrientation 往返 + 非法值 IAE。
4. value/min/max/visibleAmount/unitIncrement/blockIncrement 往返。
5. getUnitIncrement(direction)/getBlockIncrement(direction) 与字段一致。
6. setValues 一次设置四项。
7. setValueIsAdjusting 往返、setModel 往返。
8. addAdjustmentListener + setValue 触发一次 AdjustmentEvent(TRACK) 且 source 为 JScrollBar。

## 5. 深化部分（`JScrollBarTest.testDeepContracts`，钳制规则 + 增量无校验 + 模型传播）
1. setValue 钳制到 [min, max-extent]：setValue(1000)→90、setValue(-5)→0。
2. **增量无任何校验**（与值钳制对照）：setUnitIncrement(0)、setBlockIncrement(-1) 照单全收；方向切换不重置增量。ctor JScrollBar(99) IAE。
3. setValues 是原始批量 setter：请求值不钳制原样存储（value+extent 可超 max）；随后 setVisibleAmount(50) 把 extent 钳到 max-value=5。
4. 逐变更事件计数：setMinimum/setVisibleAmount/setMaximum 各 1 次 AdjustmentEvent（TRACK）。
5. 模型交换：身份保持、本身 0 事件；模型直接变异（m.setValue）经滚动条转发 1 次事件；setModel(null) NPE。
6. UI 绑定：MetalScrollBarUI / getUIClassID=="ScrollBarUI"。
