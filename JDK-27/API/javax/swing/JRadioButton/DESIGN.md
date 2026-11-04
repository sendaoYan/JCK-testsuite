# javax.swing.JRadioButton — DESIGN

## 1. API 功能
单选按钮（`extends JToggleButton`，implements `Accessible`）。核心方法分组：
- 构造器：`JRadioButton()`、`JRadioButton(String)`、`JRadioButton(String, boolean)`、`JRadioButton(Icon)`、`JRadioButton(Action)`。
- 选中态：`setSelected/isSelected`、`doClick()`（切换）。
- 分组：与 `ButtonGroup` 配合实现互斥（`group.add/remove/getSelection/clearSelection`）。
- 继承：`setMnemonic/getMnemonic`、`setActionCommand/getActionCommand`、`getModel`（返回 ButtonModel，`getGroup()` 绑定组）。

## 2. Java 用户如何使用
`ButtonGroup group = new ButtonGroup();` 然后把多个 `JRadioButton` 加进组：`group.add(r1); group.add(r2);` → `r1.setSelected(true)` 后 r2 自动取消；`group.getSelection()` 返回当前选中按钮的 model；组内 `setSelected(false)` 被拒绝（保持至少一个选中，除非 clearSelection）。`doClick()` 同样遵循互斥。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/basic/BasicFileChooserUI.java`、`JOptionPane`：多个 `JRadioButton` + `ButtonGroup` 表达单选选项。
- `~/git/jdk25u-dev/test/jdk/javax/swing/JRadioButton/`：ButtonGroup 互斥、getSelection 身份、doClick 分组行为断言。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/ButtonGroup.java`：`setSelected(model,true)` 使其它同组按钮取消选中。

## 4. 测试用例设计（黑盒部分，`JRadioButtonTest.testBlackBox`）
1. 构造文本 + 默认未选中 + 未分组 model.getGroup()==null。
2. 独立 setSelected 往返 + ItemEvent SELECTED/DESELECTED。
3. ButtonGroup 互斥链路：add/getButtonCount、one.setSelected 后 two 未选中、group.getSelection 切换、two.setSelected(true) 自动取消 one（one 触发 DESELECTED）。
4. 组内 setSelected(false) 被拒绝（活动单选保持选中）。
5. group.clearSelection + 重新选中 getSelection 身份；doClick 遵循互斥。
6. mnemonic/actionCommand 往返；装入 JFrame 后选中态保留。

## 5. 组件层深化批（`testContract`，Metal LaF）
- 完整 8 构造器矩阵；no-arg 默认 icon 为 null。
- Metal 默认：borderPainted=false、rolloverEnabled=true、focusPainted=true；
  `RadioButtonUI`/`MetalRadioButtonUI`；模型继承 ToggleButtonModel。
- 单选语义：选中项再 doClick **保持选中**且其他成员无事件（组内 radio 与组外
  toggle 的 doClick 行为差异——同一 ToggleButtonModel 因 group 存在与否而不同）。
- 选择转移恰好一对 SELECTED/DESELECTED；`getSelection()` 跟随。
- clearSelection 全清；**remove 后该成员恢复独立翻转**（两次 doClick 归零），
  组内 setSelected 切换独占、组外 setSelected 不影响组内。
- selectedIcon 身份存储。
