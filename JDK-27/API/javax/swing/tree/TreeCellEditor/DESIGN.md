# javax.swing.tree.TreeCellEditor 行为化测试设计

## 1. API 功能
树单元格编辑器接口，扩展 `CellEditor`，为 `JTree` 提供按单元格（考虑选中/展开/叶/行号）生成编辑组件。
- 组件：`getTreeCellEditorComponent(JTree,Object,selected,expanded,leaf,row)`。
- 继承 `CellEditor`：`getCellEditorValue`、`isCellEditable`、`shouldSelectCell`、`stopCellEditing`、`cancelCellEditing`、`add/removeCellEditorListener`。

## 2. Java 用户如何使用
自定义编辑器匿名实现全部方法：`getTreeCellEditorComponent` 返回一个 `JLabel` 并设置文字；`getCellEditorValue` 返回编辑值；注册/移除 `CellEditorListener`；`stopCellEditing`/`cancelCellEditing` 控制结束编辑。`DefaultTreeCellEditor` 已实现该接口。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/plaf/basic/BasicTreeUI.java`：`completeEditing`/`startEditing` 中调 `cellEditor.getTreeCellEditorComponent` 与 `cellEditor.isCellEditable`/`shouldSelectCell`。
- JDK 源码 `DefaultTreeCellEditor.java`：把 `isCellEditable`/`shouldSelectCell`/`stopCellEditing`/`cancelCellEditing` 委托给 `realEditor`（`DefaultCellEditor`）。

## 4. 测试用例设计 + 自动化实现
黑盒部分在 `TreeCellEditorTest.java`：
- 匿名实现返回固定 `JLabel` 作为编辑组件并写入文字。
- 断言 `getTreeCellEditorComponent` 返回该标签、`getCellEditorValue` 为值、`isCellEditable` false、`shouldSelectCell` true。
- `add/removeCellEditorListener` 往返；`stopCellEditing` 置位并返回 true；`cancelCellEditing` 置位；`DefaultTreeCellEditor` 实现该接口。
