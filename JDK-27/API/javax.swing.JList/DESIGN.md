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
