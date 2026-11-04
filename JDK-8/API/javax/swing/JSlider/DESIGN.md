# javax.swing.JSlider 测试设计

## 1. API 功能
滑块（`extends JComponent`，implements `SwingConstants/Accessible`）：BoundedRangeModel 区间、
大/小刻度间距、snap-to-ticks 吸附、labelTable/createStandardLabels、绘制开关（ticks/labels/track）、
反转方向。

## 2. Java 用户如何使用
`new JSlider(0, 100)` → `setMajorTickSpacing/setPaintTicks/setPaintLabels` →
`setSnapToTicks(true)` → `addChangeListener` → `setValue`；`setLabelTable` 自定义刻度标签。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JSlider.java`
（snapToTicks 在 label/notify 前做 `calcValueIfSnapToTicks`）；
`test/jdk/javax/swing/plaf/metal/MetalSliderUI/`。

## 4. 测试用例设计 + 易错细节
- 默认：0..100 value 50 / extent 0 / HORIZONTAL / 不反转 / 不画刻度标签 / **画 track** /
  不吸附 / 间距 0 / labelTable null / MetalSliderUI。
- 构造器矩阵：**(min,max) 的 value 是中点 (min+max)/2 不是 min**；(min,max,value)、
  VERTICAL、4 参、模型 5 种；ctor JSlider(99) IAE。
- **钳制级联**：setValue(±越界) 钳边界；setMinimum(200) 把 value 拖上 200；
  setMaximum(150) 低于 min 时 min/max/value 三者全部塌缩 150。
- **snap 数学**（spacing 5）：3→5、4→5、2→0；关闭后 3 原样。
- ChangeEvent 计数：setValue 1 次、**同值 0 次**；换模型 0 事件；模型变异转发 1 次；
  **setModel(null) NPE**（JSpinner 同操作是 IAE——同族对照）。
- createStandardLabels(10) 11 项含端点、(10,20) 自 start 起不含 start 以下、(0) IAE；
  Hashtable 键序不稳定——断言用 containsKey/size，不用枚举顺序。
- setLabelTable(null) 合法；setOrientation(99) IAE 且状态保持。

自动化实现位于同目录 `JSliderTest.java`；EDT 纪律 + Metal LaF 先行。
