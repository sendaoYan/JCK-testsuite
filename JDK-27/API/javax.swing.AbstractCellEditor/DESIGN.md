# javax.swing.AbstractCellEditor 设计案

## 1. API 功能
`AbstractCellEditor` 是 `CellEditor` 接口的抽象基类，为表格/树单元格编辑器提供 `CellEditorListener` 管理。方法分组：
- 查询：`isCellEditable(EventObject)`（默认 true）、`shouldSelectCell(EventObject)`（默认 true）、`getCellEditorValue()`（抽象）。
- 生命周期：`stopCellEditing()`（默认 true）、`cancelCellEditing()`。
- 监听：`addCellEditorListener`、`removeCellEditorListener`、`getCellEditorListeners`；受保护的 `fireEditingStopped/fireEditingCanceled`。

## 2. Java 用户如何使用
用户继承它实现自定义单元格编辑器，处理停止/取消事件：
```java
AbstractCellEditor ed = new AbstractCellEditor() {
    public Object getCellEditorValue() { return field.getText(); }
};
ed.addCellEditorListener(new CellEditorListener() {
    public void editingStopped(ChangeEvent e) { }
    public void editingCanceled(ChangeEvent e) { }
});
```
典型链路：`new AbstractCellEditor(){...}` → `isCellEditable` → `addCellEditorListener` → `stopCellEditing`/`cancelCellEditing` → 触发监听回调。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/DefaultCellEditor.java`：`public class DefaultCellEditor extends AbstractCellEditor`，实现表格/树编辑器（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/DefaultCellEditor.java`）。
- JDK 源码 `javax/swing/JTable.java` 与 `JTree.java` 在编辑结束时调用 `CellEditor.stopCellEditing/cancelCellEditing`。
- JDK 测试 `test/jdk/javax/swing/JTable/` 使用 `DefaultCellEditor`（间接继承 `AbstractCellEditor`）。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验 abstract、实现 `CellEditor`+`Serializable`。
- 行为（黑盒）：`testBlackBox()` 覆盖默认 `isCellEditable/shouldSelectCell/stopCellEditing` 返回值、`getCellEditorValue` 匿名实现、`add/remove/getCellEditorListeners`、`stopCellEditing` 触发 `editingStopped`、`cancelCellEditing` 触发 `editingCanceled`。
