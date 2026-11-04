# javax.swing.JList 测试设计

## 1. API 功能
列表组件（`extends JComponent`，implements `Scrollable/Accessible`，泛型 `<E>`）：三组数据构造器
（数组/Vector/ListModel）、DefaultListSelectionModel 驱动的多区间选择、anchor/lead 语义、
布局方向（VERTICAL/VERTICAL_WRAP/HORIZONTAL_WRAP）、可见行数、cellRenderer/prototypeCellValue。

## 2. Java 用户如何使用
`new JList<>(vector)` → `addListSelectionListener` → `setSelectedIndex/setSelectedIndices` →
`getSelectedValuesList` → `clearSelection`；`setModel` 换数据源（会清空选择）；`setSelectionModel`
换选择引擎；`setVisibleRowCount + setLayoutOrientation` 控制布局。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JList.java`；
`test/jdk/javax/swing/JList/`；DefaultListSelectionModel 联动（见同目录兄弟 DESIGN）。

## 4. 测试用例设计 + 易错细节
- 结构（反射）：类形态/父类/接口/方法名集合（原骨架保留为 `testShape`）。
- 默认：selectedIndex −1、anchor==lead==−1、MULTIPLE_INTERVAL_SELECTION、visibleRowCount 8、
  fixedCellWidth/Height −1、未显示时 first/lastVisibleIndex==−1、visibleRect==(0,0,0,0)。
- 事件计数矩阵：setSelectedIndex 1 次（first==last==1、anchor==lead==1）；
  **setSelectedIndices({2,0}) 3 次（清空+2 次加选，末事件区间 0..2）**；clearSelection 1 次。
- 良性无操作：setSelectedIndex(99)/setSelectedValue(absent) 不抛不改不发事件；重设同区间 0 事件。
- **setSelectedIndex(-1) 不清空既有选择且 0 事件**（与 JComboBox 的 -1 合法清空成对照）。
- getSelectedIndices 排序输出；setSelectionModel 身份+null IAE；setModel 清空选择+null IAE
  （message 含 "non null"）；setListData(array/Vector)。
- setVisibleRowCount(0) 原样、(-1) 钳到 0；setLayoutOrientation(99) IAE。
- headless 反例：setDragEnabled(true) 抛 `java.awt.HeadlessException`（JDK-27 headful 侧锁正向往返）。
- UI 绑定（Metal）：BasicListUI / getUIClassID=="ListUI"。

自动化实现位于同目录 `JListTest.java`：`check` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；EDT 纪律（EventQueue.invokeAndWait 包全部组件操作）。
