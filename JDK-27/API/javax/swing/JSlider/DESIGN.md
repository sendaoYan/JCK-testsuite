# javax.swing.JSlider 测试设计

## 1. API 功能
- 构造器：`JSlider()`、`JSlider(int)`、`JSlider(int,int)`、`JSlider(int,int,int)`、`JSlider(int,int,int,int)`、`JSlider(BoundedRangeModel)`。
- 模型：`getValue/setValue`、`getMinimum/setMinimum`、`getMaximum/setMaximum`、`getExtent/setExtent`、`getValueIsAdjusting/setValueIsAdjusting`。
- 刻度/标签：`setMajorTickSpacing/setMinorTickSpacing`、`setSnapToTicks/getSnapToTicks`、`setPaintTicks/setPaintTrack/setPaintLabels`、`setLabelTable/getLabelTable`、`createStandardLabels(int[,int])`。
- 外观/方向：`setOrientation/getOrientation`、`setInverted/getInverted`。

## 2. Java 用户如何使用
典型组合：`new JSlider(0, 100, 50)` → `setMajorTickSpacing(25)` → `setPaintTicks(true)` → `setPaintLabels(true)` → `createStandardLabels(25)` 挂标签；交互后 `getValue()` 读取当前值，`setSnapToTicks(true)` 让值吸附到刻度。

## 3. 开源代码实际用法调研
- JDK 自带测试 `test/jdk/javax/swing/plaf/metal/MetalSliderUI/Test6657026.java` 与 `plaf/basic/BasicSliderUI/bug4220108.java`：构造 JSlider 并验证 UI 绘制/刻度行为。
- JDK 源码 `javax.swing.JSlider.updateLabelUIs`：读取 labelTable 并更新 UI，说明 `setLabelTable` 与 `createStandardLabels` 的协作方式。
- 常见桌面应用用 `setMajorTickSpacing`+`setPaintLabels` 做带刻度音量/进度滑块。

## 4. 测试用例设计 + 自动化实现
黑盒覆盖：默认 value/min/max/orientation/刻度/吸附/绘制/反转/范围/标签表状态；各构造器；`setValue` 夹取；`setMinimum/setMaximum`；刻度、吸附、绘制、反转、标签表、值调整中、范围的往返；`createStandardLabels` 数量与键；非法 orientation 抛 `IllegalArgumentException`。见 `JSliderTest.java` 的 `testBlackBox()`。


## 5. 深化部分（`JSliderTest.testDeepContracts`，钳制级联 + 吸附数学 + ChangeEvent 计数）
1. 双参构造器 value 取中点 (min+max)/2，不是 min。
2. 钳制级联：setValue(200)→100、setValue(-5)→0；setMinimum(200) 把 value 拖上 200；setMaximum(150) 低于 min 时 min/max/value 三者全部塌缩到 150。
3. snap-to-ticks 精确数学（minorTickSpacing=5）：setValue(3)→5、setValue(4)→5、setValue(2)→0；关闭后 3 原样。
4. ChangeEvent 计数：setValue 1 次、同值 0 次；换模型本身 0 事件、模型变异转发 1 次、同模型重设 0 次；setModel(null) NPE（与 JSpinner 的 IAE 对照）。
5. setLabelTable(null) 合法；createStandardLabels(0) IAE；ctor JSlider(99)/setOrientation(99) IAE 且状态保持。
6. UI 绑定：MetalSliderUI / getUIClassID=="SliderUI"。
