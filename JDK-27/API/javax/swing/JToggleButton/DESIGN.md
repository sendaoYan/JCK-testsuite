# javax.swing.JToggleButton — DESIGN

## 1. API 功能
两态切换按钮（`extends AbstractButton`，implements `Accessible`），JCheckBox/JRadioButton 的父类。核心方法分组：
- 构造器：`JToggleButton()`、`JToggleButton(String)`、`JToggleButton(String, boolean)`、`JToggleButton(Icon)`、`JToggleButton(Icon, boolean)`、`JToggleButton(Action)`。
- 选中态：`setSelected/isSelected`、`doClick()`（切换）、`getModel`（返回 `JToggleButton.ToggleButtonModel`）。
- 继承：`setText/setMnemonic/setIcon/setEnabled` 等 AbstractButton 方法。
- 事件：ItemEvent（选中/取消）、PropertyChangeEvent（"text"、"mnemonic" 等）。

## 2. Java 用户如何使用
`new JToggleButton("Toggle")` → `setSelected(true)` 或 `doClick()` 切换；`getModel()` 返回 ToggleButtonModel 可经 `model.setSelected` 反向驱动按钮。`addItemListener` 记录 SELECTED/DESELECTED；`addPropertyChangeListener` 观察 "text"/"mnemonic" 变化。禁用后 `doClick()` 不切换。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JToggleButton.java`：内部 `ToggleButtonModel extends DefaultButtonModel`（`setPressed` 时切换 SELECTED）。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/basic/BasicFileChooserUI.java`：JToggleButton 用作工具条切换项。
- `~/git/jdk25u-dev/test/jdk/javax/swing/JToggleButton/`：选中态、ItemEvent、doClick 断言。

## 4. 测试用例设计（黑盒部分，`JToggleButtonTest.testBlackBox`）
1. 构造器（text/Icon/text+selected）+ 默认未选中 + model instanceof ToggleButtonModel。
2. 选中链：setSelected 往返、model.isSelected 镜像、model.setSelected 反向驱动按钮。
3. ItemEvent 记录：select 一次 SELECTED、冗余 setSelected 静默、deselect DESELECTED；doClick 切换 + 一次 ItemEvent。
4. setText 触发 PropertyChangeEvent（old/new 值）+ setMnemonic 触发 "mnemonic"。
5. 禁用链：禁用 doClick 不切换；装入 JFrame 后 displayable 断言。

## 5. 组件层深化批（`testContract`，Metal LaF）
- 完整 8 构造器矩阵（含 text+icon+selected、icon+selected 变体）。
- UI 身份：`ToggleButtonUI`/`MetalToggleButtonUI`；**rolloverEnabled 默认 false**
  （与 JButton 的 true 相反——易错）。
- doClick 双事件：ActionEvent + ItemEvent 成对（SELECTED→DESELECTED），
  `ItemEvent.getItem()==按钮本身`（JToggleButton.fireItemStateChanged 重发）。
- 状态机边沿：armed+pressed 不翻转；**release 时翻转**；再 press 保持、再 release 再翻
  （翻转只发生在 armed 且 release 的边沿）。
- 组内互斥：点击另一成员转移选择、`getSelection()==胜者模型`。
- selectedIcon/pressedIcon 身份存储。
