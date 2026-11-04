# javax.swing.JToggleButton 测试设计

## 1. API 功能
两态按钮（按下/弹起）：8 个构造器、ToggleButtonModel（release 翻转语义）、
ToggleButtonUI 委托、组内互斥、doClick 翻转并触发 Action+Item 事件。

## 2. Java 用户如何使用
工具栏"保持按下"型按钮：`JToggleButton t = new JToggleButton("Bold"); t.addItemListener(...)`；
多个互斥态放进 ButtonGroup；状态由 `isSelected()` 读出。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`javax/swing/JToggleButton.java`；`test/jdk/javax/swing/JToggleButton/`；
JCheckBox/JRadioButton 都继承它。

## 4. 测试用例设计 + 易错细节
- 8 构造器矩阵（含 selected=true 变体、icon 变体）。
- 模型：ToggleButtonModel；UI：`ToggleButtonUI`/`MetalToggleButtonUI`；
  **rolloverEnabled 默认 false**（与 JButton 的 true 相反——易错）。
- doClick：第一次选中（1 ActionEvent + 1 ItemEvent SELECTED），第二次取消
  （DESELECTED）；`ItemEvent.getItem()==按钮本身`（JToggleButton.fireItemStateChanged 重发）。
- 状态机：armed+pressed 不翻转；**release 时翻转**；再 press 不变、再 release 再翻转
  （翻转只发生在"armed 且 release"边沿）。
- 组内互斥：点击另一成员转移选择；`getSelection()==胜者模型`。
- selectedIcon/pressedIcon 身份存储。

自动化实现位于同目录 `*Test.java`（headless，Metal LaF + EDT）。
