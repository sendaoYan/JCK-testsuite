# javax.swing.JComboBox 测试设计

## 1. API 功能
组合框（`extends JComponent`，implements `ItemSelectable/ListDataListener/ActionListener/Accessible`）：
数组/Vector/ComboBoxModel 构造器、DefaultComboBoxModel 数据驱动、选择语义、可编辑模式、
弹出配置（maximumRowCount/lightWeightPopup）、renderer/editor 双轨。

## 2. Java 用户如何使用
`new JComboBox<>(new String[]{...})` 首项自动选中 → `setSelectedIndex/setSelectedItem` →
`addActionListener/addItemListener` → `addItem/removeItemAt` 增删项；可编辑模式配合 getEditor。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JComboBox.java`（checkMutableComboBoxModel）；
`test/jdk/javax/swing/JComboBox/`。

## 4. 测试用例设计 + 易错细节
- 默认（空组合框）：count 0 / idx −1 / item null / getSelectedObjects 长度 0 / maxRowCount 8 /
  不可编辑 / lightWeight true / actionCommand "comboBoxChanged" / DefaultComboBoxModel /
  renderer+editor 非空 / MetalComboBoxUI。
- 构造器：数组、Vector、模型三种全部落 DefaultComboBoxModel（可变）；数组构造器自动选 0。
- ActionEvent 计数：每次真实选择变化恰好 1 次、命令恒 "comboBoxChanged"；**setEditable 0 次**；
  ItemEvent 序列精确 "DESELECTED:a;SELECTED:c;"。
- **setSelectedIndex(-1) 合法清空**（JList 对照）；setSelectedIndex(99)/(-2) IAE；
  非可编辑 setSelectedItem(absent) 拒绝且 0 事件。
- 索引矩阵：getItemAt(±越界) null 不抛；removeItemAt(±)/insertItemAt(越界) AIOOBE。
- removeAllItems：count 0、idx −1、恰好 1 次 ActionEvent。
- **非 Mutable 模型**：addItem/removeItem/removeAllItems/insertItemAt 全部抛 RuntimeException
  ("non-Mutable data model")，计数不变。
- **setModel(null) NPE 且实例被置脏**（model 字段已 null，后续 getItemAt 直接 NPE——不可复用）。
- addActionListener(null)/removeItemListener(null) 无异常容忍；setMaximumRowCount(0) 合法。

自动化实现位于同目录 `JComboBoxTest.java`；EDT 纪律 + Metal LaF 先行。
