# javax.swing.JProgressBar 测试设计

## 1. API 功能
- 构造器：`JProgressBar()`、`JProgressBar(int)`、`JProgressBar(int,int)`、`JProgressBar(int,int,int)`、`JProgressBar(BoundedRangeModel)`。
- 模型：`getModel/setModel`、`getValue/setValue`、`getMinimum/setMinimum`、`getMaximum/setMaximum`、`getPercentComplete()`。
- 外观/方向：`setOrientation/getOrientation`、`setIndeterminate/isIndeterminate`、`setStringPainted/isStringPainted`、`setString/getString`、`setBorderPainted/isBorderPainted`。

## 2. Java 用户如何使用
典型组合：`new JProgressBar(0, 100)` → 进度中 `setValue(n)` 或未知时长 `setIndeterminate(true)`；完成后 `setValue(max)`、`setIndeterminate(false)`。文本用 `setStringPainted(true)` + `setString("…")`；方向切换 `setOrientation(VERTICAL)`。

## 3. 开源代码实际用法调研
- JDK 自带测试 `test/jdk/javax/swing/JProgressBar/TestProgressBarUI.java`：`p1.setValue(50); p1.setStringPainted(true);` 验证进度条 UI 表现。
- JDK 自带测试 `test/jdk/javax/swing/JProgressBar/RightLeftOrientation.java`：`p.setStringPainted(paintStr); p.setValue(value);` 验证 RTL/方向组合。
- JDK 源码 `javax.swing.SwingWorker` 教程示例用 `setMaximum`/`setValue`/`setIndeterminate` 驱动进度条。

## 4. 测试用例设计 + 自动化实现
黑盒覆盖：默认 value/min/max/orientation/indeterminate/stringPainted/borderPainted；各构造器（含 BoundedRangeModel）；`setValue` 的 min/max 夹取；`setMinimum/setMaximum`；`getPercentComplete` 的精确值（0.25/0.5/1.0/0.0）；`setIndeterminate`、`setStringPainted`、`setString`（含复位后非 null）、`setBorderPainted`、`setOrientation` 往返与非法值 `IllegalArgumentException`。见 `JProgressBarTest.java` 的 `testBlackBox()`。


## 5. 深化部分（`JProgressBarTest.testDeepContracts`，派生字符串 + 事件计数 + null 模型怪癖）
1. 派生字符串：默认 "0%"、setValue(50)→"50%"、setValue(500) 钳到 100→"100%"（percent 1.0）、setValue(-9)→"0%"；setString("done") 覆盖派生且跨 setValue 稳定、setString(null) 恢复派生 "75%"。
2. min-max 构造器 value 从 min 起。
3. ChangeEvent 计数：setValue 1 次、同值 0 次、换模型 0 次、模型变异 1 次转发、同模型重设 0 次。
4. **setModel(null) 被接受（getModel()==null），后续 setValue 抛 NPE**（与 JSlider 的 setModel(null) NPE、JSpinner 的 IAE 形成三方对照——同族组件对同一操作三种行为）。
5. setOrientation(99) IAE 且状态保持；setIndeterminate 往返不动 stringPainted。
6. UI 绑定：MetalProgressBarUI / getUIClassID=="ProgressBarUI"。
