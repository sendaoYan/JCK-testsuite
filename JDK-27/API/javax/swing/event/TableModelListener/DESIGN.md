# javax.swing.event.TableModelListener 测试设计

## 1. API 功能

`TableModelListener` 监听表格数据模型（`TableModel`）变化，继承 `java.util.EventListener`，一个回调：

- `void tableChanged(TableModelEvent e)`：模型数据或结构变化时触发。

事件 `TableModelEvent` 携带 `getFirstRow()/getLastRow()/getColumn()/getType()`，常量 `INSERT/UPDATE/DELETE/HEADER_ROW/ALL_COLUMNS`。构造器有 5 种重载（整表/单行/区间/区间+列/区间+列+类型）。

## 2. Java 用户如何使用

构造：实现接口并注册到 `TableModel`：

```java
TableModel model = new AbstractTableModel() {
    public int getRowCount() { return 10; }
    public int getColumnCount() { return 4; }
    public Object getValueAt(int r, int c) { return r * 100 + c; }
};
TableModelListener l = new TableModelListener() {
    public void tableChanged(TableModelEvent e) { /* 模型变化 */ }
};
((AbstractTableModel) model).addTableModelListener(l);
```

事件对象由 `AbstractTableModel` 的 `fireTableDataChanged/fireTableRowsInserted/fireTableRowsUpdated/fireTableRowsDeleted/fireTableCellUpdated/fireTableChanged` 构造。

典型组合链路：`addTableModelListener → fireTableRowsInserted(2,5) → tableChanged（type=INSERT，first=2，last=5）→ 读 getType/getFirstRow/getLastRow 增量刷新`。

## 3. 开源代码实际用法调研

1. JDK `JTable` 声明实现 `TableModelListener`（`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JTable.java:281`），`tableChanged`（`:4453`）处理整表/结构/增量变化：
   ```java
   public void tableChanged(TableModelEvent e) {
       if (e == null || e.getFirstRow() == TableModelEvent.HEADER_ROW) { ... }
       ...
   }
   ```
2. `JTable` 内部无障碍类也实现 `tableChanged`（同文件 `:6925`）广播可见数据变化。
3. `AbstractTableModel.fireTableRowsInserted(...)` 等内部 `new TableModelEvent(this, firstRow, lastRow, ALL_COLUMNS, INSERT)` 派发，是 `TableModelEvent` 五种构造的真实来源。

## 4. 测试用例设计 + 自动化实现

黑盒设计（`TableModelListenerTest.testBlackBox()`）：
- 匿名实现记录事件。
- 构造四种 `TableModelEvent`（INSERT/UPDATE/DELETE/整表默认），调用 `tableChanged`。
- 断言 `getSource/getFirstRow/getLastRow/getColumn/getType` round-trip；整表事件默认 `type=UPDATE`、`first=0`、`last=Integer.MAX_VALUE`、`column=ALL_COLUMNS`；各 type 常量互异。

白盒保留原有结构断言，新增 `tableChanged(TableModelEvent)` 返回 void/抽象与 `declared method count == 1`。
