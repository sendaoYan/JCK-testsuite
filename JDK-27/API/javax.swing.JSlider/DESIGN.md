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
