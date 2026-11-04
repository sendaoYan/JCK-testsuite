# javax.swing.JRadioButton 测试设计

## 1. API 功能
单选钮（JToggleButton 子类）：8 个构造器、RadioButtonUI 委托、ButtonGroup 内
"点击已选中项保持选中"的单选语义、移出组后恢复翻转行为。

## 2. Java 用户如何使用
`ButtonGroup g = new ButtonGroup(); JRadioButton a=..., b=...; g.add(a); g.add(b);`
用户点击切换；`g.getSelection()` 拿选中模型、`g.clearSelection()` 清空。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`javax/swing/JRadioButton.java`；`test/jdk/javax/swing/JRadioButton/`；
MetalRadioButtonUI 提供圆形图标。

## 4. 测试用例设计 + 易错细节
- 8 构造器矩阵；no-arg 默认 icon 为 null。
- Metal 默认：borderPainted=false、rolloverEnabled=true、focusPainted=true；
  `RadioButtonUI`/`MetalRadioButtonUI`；模型继承 ToggleButtonModel。
- 单选语义：选中项再 doClick **保持选中**且同组其他成员无事件（组内 radio 与
  组外 toggle 的 doClick 行为差异——同一 ToggleButtonModel 因 group 而不同，核心易错点）。
- 选择转移：点击另一成员时原成员收 DESELECTED、新成员收 SELECTED，各恰好一次；
  `g.getSelection()` 跟随。
- clearSelection 全清；**remove 后该成员恢复独立翻转**（两次 doClick 归零），
  组内其余成员互斥不受影响；组内 setSelected 切换独占。
- selectedIcon 身份存储。

自动化实现位于同目录 `*Test.java`（headless，Metal LaF + EDT）。
