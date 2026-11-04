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
