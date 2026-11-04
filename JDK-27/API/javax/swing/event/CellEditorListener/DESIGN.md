# javax.swing.event.CellEditorListener 测试设计

## 1. API 功能

`CellEditorListener` 监听表格/树单元格编辑器（`javax.swing.CellEditor`）的编辑生命周期结束，继承 `java.util.EventListener`，两个回调：

- `void editingStopped(ChangeEvent e)`：编辑正常结束（提交值）时触发。
- `void editingCanceled(ChangeEvent e)`：编辑被取消（丢弃值）时触发。

事件是通用的 `ChangeEvent`（继承 `java.util.EventObject`，构造 `ChangeEvent(Object source)`），事件源是发生变化的 `CellEditor`。

## 2. Java 用户如何使用

构造：实现接口并注册到单元格编辑器：

```java
CellEditor editor = table.getCellEditor();
CellEditorListener l = new CellEditorListener() {
    public void editingStopped(ChangeEvent e)   { /* 提交 */ }
    public void editingCanceled(ChangeEvent e)  { /* 取消 */ }
};
editor.addCellEditorListener(l);
```

事件对象由编辑器内部构造：`new ChangeEvent(this)` 后分发。

典型组合链路：`table.editCellAt(row, col) → editor.addCellEditorListener(l) → 用户 Enter → editingStopped(new ChangeEvent(editor)) → 读 e.getSource() 得编辑器实例`；取消路径（Esc）走 `editingCanceled`。两方法分别对应提交与取消两条互斥路径。

## 3. 开源代码实际用法调研

1. JDK 实现 `JTable` 声明实现该接口并注册到编辑器（`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JTable.java:281,3602`）：
   ```java
   public class JTable extends JComponent implements TableModelListener, ...
       TableColumnModelListener, ListSelectionListener, CellEditorListener ...
   ...
   editor.addCellEditorListener(this);
   ```
2. `JTable` 内部 `editingStopped(ChangeEvent)` 把编辑器值写回模型、`editingCanceled(ChangeEvent)` 丢弃改动（同文件，`Editing` 逻辑区域），是两方法的典型实现。
3. `AbstractCellEditor`（`javax/swing/AbstractCellEditor.java`）提供 `addCellEditorListener`/`fireEditingStopped`/`fireEditingCanceled`，内部 `new ChangeEvent(this)` 后分发，是 `ChangeEvent` 的构造来源。

## 4. 测试用例设计 + 自动化实现

黑盒设计（`CellEditorListenerTest.testBlackBox()`）：
- 匿名实现记录最近回调名与事件对象。
- 构造两个 `ChangeEvent`（共享 source），分别调用 `editingStopped`/`editingCanceled`。
- 断言各回调被触发、事件实例一致、`getSource()` round-trip，且两次事件实例互异。

白盒保留原有结构断言，新增两方法签名/返回类型/抽象性与 `declared method count == 2`。
