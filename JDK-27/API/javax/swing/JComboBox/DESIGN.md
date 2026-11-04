# javax.swing.JComboBox 黑盒行为测试设计

## 1. API 功能
JComboBox<E> 是下拉组合框，继承 JComponent，实现 ItemSelectable、ListDataListener、ActionListener、Accessible，内部以 ComboBoxModel（默认 DefaultComboBoxModel）驱动。

- 构造器：`JComboBox()`、`JComboBox(E[])`、`JComboBox(Vector<E>)`、`JComboBox(ComboBoxModel<E>)`（数组/向量构造默认选中第一项）。
- 条目：`addItem/insertItemAt/removeItem/removeItemAt/removeAllItems/getItemCount/getItemAt`。
- 选择：`getSelectedItem/setSelectedItem`、`getSelectedIndex/setSelectedIndex`、`getSelectedObjects`（ItemSelectable）。
- 可编辑：`isEditable/setEditable`（默认 false）。
- 行数/原型：`getMaximumRowCount/setMaximumRowCount`（默认 8）、`getPrototypeDisplayValue/setPrototypeDisplayValue`。
- Action：`getActionCommand/setActionCommand`（默认 "comboBoxChanged"）。
- 弹出：`isLightWeightPopupEnabled/setLightWeightPopupEnabled`、`isPopupVisible/setPopupVisible`、`showPopup/hidePopup`。
- 模型/渲染/编辑器：`getModel/setModel`、`getRenderer/setRenderer`、`getEditor/setEditor`。
- 事件：`addItemListener/removeItemListener`、`addActionListener/removeActionListener`、`addPopupMenuListener/removePopupMenuListener`。

## 2. Java 用户如何使用
- `JComboBox<String> cb = new JComboBox<>(new String[]{"a","b","c"});` 初始化并默认选中 "a"。
- `cb.addItem("d");` 追加；`cb.setSelectedItem("b");` 改变选择。
- `cb.setEditable(true);` 允许输入；`cb.setMaximumRowCount(6);` 限制下拉行数。
- `cb.addItemListener(e -> ...)` 监听选择变化。

## 3. 开源代码实际用法调研
JDK 自带测试（`~/git/jdk25u-dev/test/jdk`）：
- `test/jdk/java/awt/Container/OpenedPopupFrameDisposal.java` 用 `new JComboBox<>()` + `addItem("one")/addItem("two")` 驱动条目。
- `test/jdk/java/awt/Window/setLocRelativeTo/SetLocationRelativeToTest.java` 用 JComboBox 弹窗定位。
- `test/jdk/java/awt/Component/PaintGlitchTest/PaintGlitchTest.java` 用 JComboBox 做绘制回归。
以上来源为 `~/git/jdk25u-dev/test/jdk`。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`JComboBoxTest.testBlackBox()`）：
1. 默认值：空 / selectedIndex -1 / selectedItem null / 不可编辑 / maximumRowCount 8 / actionCommand 默认 / DefaultComboBoxModel。
2. 数组/向量构造默认选中第一项。
3. addItem 自动选中首项、insertItemAt/getItemAt/removeItemAt/removeItem/removeAllItems 组合链路。
4. setSelectedItem/getSelectedItem/getSelectedIndex 往返、非可编辑拒绝未知项、setSelectedIndex(-1) 清空、越界 IAE。
5. editable/maximumRowCount/prototypeDisplayValue/actionCommand/lightWeightPopupEnabled 往返。
6. setModel/getModel 往返（新模型默认选中第一项）。
7. ItemListener 选择变化触发 DESELECTED+SELECTED 序列。
8. getSelectedObjects 返回单选结果。
