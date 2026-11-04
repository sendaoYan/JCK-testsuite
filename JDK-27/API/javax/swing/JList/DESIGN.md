# javax.swing.JList — DESIGN

## 1. API 功能
列表（`extends JComponent`，implements `Scrollable/Accessible`，泛型 `<E>`）。核心方法分组：
- 构造器/模型：`JList(E[])`、`JList(Vector)`、`JList(ListModel)`、`setModel/getModel`、`setListData(Object[])`。
- 选择：`setSelectedIndex/getSelectedIndex`、`setSelectedIndices/getSelectedIndices`、`setSelectedValue/getSelectedValue`、`getSelectedValuesList`、`isSelectedIndex`、`clearSelection/isSelectionEmpty`、`getMinSelectionIndex/getMaxSelectionIndex`、`getSelectedValue`。
- 选择模型/模式：`setSelectionModel/getSelectionModel`、`setSelectionMode/getSelectionMode`（ListSelectionModel.SINGLE_SELECTION 等）。
- 布局：`setVisibleRowCount/getVisibleRowCount`、`setLayoutOrientation/getLayoutOrientation`（VERTICAL/VERTICAL_WRAP/HORIZONTAL_WRAP）。
- 其他：`setPrototypeCellValue/getPrototypeCellValue`、`ensureIndexIsVisible(int)`、`getCellRenderer`。

## 2. Java 用户如何使用
`new JList<>(new String[]{"a","b","c"})` → `setSelectionMode(SINGLE_SELECTION)` → `addListSelectionListener(e -> ...)` → `setSelectedIndex(0)` → `getSelectedValue()` → `clearSelection()`；`setVisibleRowCount(7)` + `setLayoutOrientation(VERTICAL_WRAP)` 控制显示。`setModel(null)`/`setSelectionModel(null)` → IAE。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JColorChooser.java`、`plaf/basic/BasicFileChooserUI.java`：`JList` + `ListSelectionListener` 驱动选项选择。
- `~/git/jdk25u-dev/test/jdk/javax/swing/JList/`：多选、选择模型替换、监听器触发语义断言。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JList.java`：`fireSelectionValueChanged` 以 JList 为事件 source。

## 4. 测试用例设计（黑盒部分，`JListTest.testBlackBox`）
1. Vector/数组构造器模型内容（size/element/getCellRenderer instanceof ListCellRenderer）。
2. 初始选择为空；setSelectedIndex 触发 ListSelectionEvent（first==last==2、source==list、不调整）。
3. setSelectedIndices {0,3} 多选 + min/max + getSelectedValuesList；clearSelection 复位。
4. visibleRowCount/layoutOrientation 往返 + 非法 77 IAE。
5. 选择模型替换后监听器仍触发 + setSelectionModel(null) IAE；setSelectionMode SINGLE 后 setSelectedIndices 只保留最后一个。
6. setModel 清空选择 + setSelectedValue 往返 + setModel(null) IAE；setListData 替换固定模型；prototypeCellValue 往返。

## 5. 深化部分（`JListTest.testDeepContracts`，事件计数矩阵 + 钳制/无操作矩阵）
1. 未显示列表的几何默认：first/lastVisibleIndex==−1、visibleRect==(0,0,0,0)、ensureIndexIsVisible 无异常。
2. 选择状态默认：anchor==lead==−1、MULTIPLE_INTERVAL_SELECTION、visibleRowCount==8、fixedCellWidth/Height==−1、dragEnabled==false（headful 下 setDragEnabled(true) 往返成功；headless 下抛 HeadlessException——JDK-8 侧锁定该反例）。
3. 精确事件计数：setSelectedIndex 1 次；setSelectedIndices({2,0}) 3 次（清空+2 次加选，末事件区间 0..2）；clearSelection 1 次；getSelectedIndices 排序输出 [0,2]。
4. 良性无操作：setSelectedIndex(99)/setSelectedValue(absent) 不抛、不改、不触发事件；重设同区间 0 事件。
5. **setSelectedIndex(-1) 不清空既有选择且 0 事件**（DefaultListSelectionModel 特性，与 JComboBox 的 -1 合法清空形成对照）。
6. setVisibleRowCount(0) 原样存储、(-1) 钳到 0（无异常、无校验抛出，负值静默钳制）。
7. setModel(null) IAE 且 message 含 "non null"；setListData(Vector) 变体；UI 绑定 BasicListUI/getUIClassID=="ListUI"/updateUI。

