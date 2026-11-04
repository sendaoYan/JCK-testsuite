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

## 5. 深化部分（`JComboBoxTest.testDeepContracts`，ActionEvent 计数 + 边界矩阵 + 固定模型拒绝）
1. ActionEvent 精确计数：setSelectedIndex/setSelectedItem/setSelectedIndex(-1) 各触发 1 次、命令恒为 "comboBoxChanged"、source 为组合框；setEditable 不触发；addActionListener(null) 无异常容忍。
2. 编辑器：可编辑时 getEditor 非空；setEditor(同一实例) 身份保持。setMaximumRowCount(0) 合法（无校验）；setRenderer 身份往返。
3. 索引边界矩阵：getItemAt(99)/(-1) 返回 null 不抛；removeItemAt(99)/(-1)、insertItemAt(x,99) 抛 ArrayIndexOutOfBoundsException，计数不变。
4. removeAllItems 触发恰好 1 次 ActionEvent 并清空选择。
5. **非 Mutable 模型**（自定义固定 ComboBoxModel）：addItem/removeItem/removeAllItems/insertItemAt 均抛 RuntimeException("Cannot use this method with a non-Mutable data model")，计数不变；而数组/Vector/默认构造器的模型都是 DefaultComboBoxModel（可变）。
6. setModel(null) 抛 NPE（注意：实例被置脏，不可复用——后续 getItemAt 直接 NPE，测试用一次性实例）。
7. UI 绑定：MetalComboBoxUI / getUIClassID=="ComboBoxUI"。
