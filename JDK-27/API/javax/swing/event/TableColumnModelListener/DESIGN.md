# javax.swing.event.TableColumnModelListener 测试设计

## 1. API 功能

`TableColumnModelListener` 监听表格列模型（`TableColumnModel`）结构变化，继承 `java.util.EventListener`，五个回调：

- `columnAdded(TableColumnModelEvent e)`：列被加入。
- `columnRemoved(TableColumnModelEvent e)`：列被移除。
- `columnMoved(TableColumnModelEvent e)`：列被移动。
- `columnMarginChanged(ChangeEvent e)`：列边距变化。
- `columnSelectionChanged(ListSelectionEvent e)`：列选择变化。

事件：`TableColumnModelEvent` 携带 `getFromIndex()/getToIndex()` 与 `getSource()`；`ChangeEvent` 与 `ListSelectionEvent` 分别携带各自信息。

## 2. Java 用户如何使用

构造：实现接口并注册到 `TableColumnModel`：

```java
TableColumnModel model = new DefaultTableColumnModel();
TableColumnModelListener l = new TableColumnModelListener() {
    public void columnAdded(TableColumnModelEvent e)   { /* 加列 */ }
    public void columnRemoved(TableColumnModelEvent e) { /* 删列 */ }
    public void columnMoved(TableColumnModelEvent e)   { /* 移动 */ }
    public void columnMarginChanged(ChangeEvent e)     { /* 边距 */ }
    public void columnSelectionChanged(ListSelectionEvent e) { /* 选择 */ }
};
model.addColumnModelListener(l);
```

事件对象由 `DefaultTableColumnModel` 的 `fireColumnAdded/fireColumnRemoved/fireColumnMoved/fireColumnMarginChanged/fireColumnSelectionChanged` 构造。

典型组合链路：`addColumnModelListener → addColumn → columnAdded（读 getFromIndex/getToIndex）→ moveColumn → columnMoved → setColumnMargin → columnMarginChanged`。

## 3. 开源代码实际用法调研

1. JDK `JTableHeader` 声明实现 `TableColumnModelListener`（`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/table/JTableHeader.java:89`）并注册（`:569`）：
   ```java
   public class JTableHeader extends JComponent implements TableColumnModelListener, Accessible ...
   ...
   public void columnAdded(TableColumnModelEvent e) { resizeAndRepaint(); }
   ```
2. `JTable` 也实现 `TableColumnModelListener`（`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JTable.java:281-282`），注册到 `columnModel`（`:3817`）。
3. `DefaultTableColumnModel` 内部 `fireColumnAdded(...)` 等构造 `TableColumnModelEvent(this, from, to)` 派发，是 `getFromIndex/getToIndex` 的权威来源。

## 4. 测试用例设计 + 自动化实现

黑盒设计（`TableColumnModelListenerTest.testBlackBox()`）：
- 匿名实现记录最近回调名与事件。
- 构造三个 `TableColumnModelEvent`（source=`DefaultTableColumnModel`，不同 from/to）、一个 `ChangeEvent`、一个 `ListSelectionEvent`，分别调用五个回调。
- 断言回调名正确、`getFromIndex/getToIndex/getSource`、`ListSelectionEvent` 的 `getFirstIndex/getLastIndex` 全部 round-trip。

白盒保留原有结构断言，新增五方法签名/抽象性与 `declared method count == 5`。
