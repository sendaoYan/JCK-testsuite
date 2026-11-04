# javax.swing.JProgressBar 测试设计

## 1. API 功能
进度条（`extends JComponent`，implements `SwingConstants/Accessible`）：BoundedRangeModel 区间、
完成百分比、派生百分比字符串 vs 自定义字符串、stringPainted/indeterminate 绘制开关、方向。

## 2. Java 用户如何使用
`new JProgressBar()` → `setValue(60)` → `setStringPainted(true)` 显示 "60%" →
长任务里 `setIndeterminate(true/false)` 切换摆动/确定模式。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JProgressBar.java`
（getString 无自定义串时由 percent 派生）；`test/jdk/javax/swing/JProgressBar/`。

## 4. 测试用例设计 + 易错细节
- 默认：0..100 value 0 / HORIZONTAL / stringPainted false / indeterminate false /
  getString "0%" / percent 0.0 / MetalProgressBarUI。
- 派生字符串矩阵：setValue(50)→"50%"；**setValue(500) 先钳到 100 再派生 "100%"**（percent 1.0）；
  setValue(-9)→"0%"；setString("done") 覆盖派生且跨 setValue 稳定；**setString(null) 恢复派生**。
- (min,max) 构造器 value 从 min 起；4 参与模型构造器全覆盖。
- ChangeEvent 计数：setValue 1 次、同值 0 次、换模型 0 次、模型变异转发 1 次、同模型重设 0 次。
- **setModel(null) 被接受（getModel()==null），后续 setValue 抛 NPE**——
  与 JSlider 的 setModel(null) NPE、JSpinner 的 IAE 构成同族三方对照。
- setOrientation(99) IAE（"99 is not a legal orientation"）且状态保持；
  setIndeterminate 往返不动 stringPainted。

自动化实现位于同目录 `JProgressBarTest.java`；EDT 纪律 + Metal LaF 先行。
