# javax.swing.tree.DefaultTreeCellEditor 行为化测试设计

## 1. API 功能
`JTree` 默认的 `TreeCellEditor`，实现 `TreeCellEditor`、`ActionListener`、`TreeSelectionListener`，把编辑委托给内部真实编辑器（默认为文本框 `DefaultCellEditor`）。
- 构造器：`DefaultTreeCellEditor(JTree, DefaultTreeCellRenderer)`、`DefaultTreeCellEditor(JTree, DefaultTreeCellRenderer, TreeCellEditor)`。
- 颜色/字体：`setBorderSelectionColor/getBorderSelectionColor`、`setFont/getFont`。
- 编辑接口：`getTreeCellEditorComponent`、`getCellEditorValue`、`isCellEditable`、`shouldSelectCell`、`stopCellEditing`、`cancelCellEditing`。
- 监听/事件：`addCellEditorListener`、`removeCellEditorListener`、`getCellEditorListeners`、`valueChanged`、`actionPerformed`。

## 2. Java 用户如何使用
构造与多步链路：`new DefaultTreeCellEditor(new JTree(), new DefaultTreeCellRenderer())` → `setFont(f)` → `setBorderSelectionColor(c)` → `getTreeCellEditorComponent(tree, "hello", ...)` → `getCellEditorValue()` → `isCellEditable(null)`/`shouldSelectCell(null)` → `stopCellEditing()`/`cancelCellEditing()`。

## 3. 开源代码实际用法调研
- JDK 源码 `BasicTreeUI#createDefaultCellEditor()` 返回 `new DefaultTreeCellEditor(tree, (DefaultTreeCellRenderer)renderer)`；UI 在其上调用 `getTreeCellEditorComponent`/`isCellEditable`/`stopCellEditing` 完成编辑会话。
- JDK 源码 `DefaultTreeCellEditor` 构造器内 `realEditor = createTreeCellEditor()`（缺省为 `DefaultCellEditor(new DefaultTextField(...))`），并把 `isCellEditable/shouldSelectCell/stopCellEditing/cancelCellEditing` 全部委托给 `realEditor`。

## 4. 测试用例设计 + 自动化实现
黑盒部分在 `DefaultTreeCellEditorTest.java`：
- 默认字体为 null 与 `setFont/getFont` 往返、`setBorderSelectionColor/getBorderSelectionColor` 往返。
- `getTreeCellEditorComponent` 返回非空 `Container` 且 `getCellEditorValue()=="hello"`；`isCellEditable(null)` 与 `shouldSelectCell(null)` 均为 true。
- 注册 `CellEditorListener` 断言 `stopCellEditing()` 返回 true 且 `editingStopped` 触发一次、`cancelCellEditing()` 触发 `editingCanceled`；`getCellEditorListeners`/`removeCellEditorListener` 往返。
- `valueChanged`/`actionPerformed` 无编辑会话时不抛；3 参构造器用自定义编辑器，`getCellEditorValue()` 委托返回自定义值。
