# TableColumnModelEvent 行为测试设计

## 1. API 功能
`javax.swing.event.TableColumnModelEvent` 是表格列模型变更事件，继承 `java.util.EventObject`。

- **构造器**：`TableColumnModelEvent(TableColumnModel source, int fromIndex, int toIndex)`。
- **查询**：`getFromIndex()`、`getToIndex()`、继承的 `getSource()`。

## 2. Java 用户如何使用
`TableColumnModel` 实现（如 `DefaultTableColumnModel`）在列增/删/移动时派发，用户实现 `TableColumnModelListener`：

```java
model.addColumnModelListener(new TableColumnModelListener() {
    public void columnAdded(TableColumnModelEvent e) {
        int from = e.getFromIndex(), to = e.getToIndex();
        TableColumnModel m = (TableColumnModel) e.getSource();
    }
    public void columnRemoved(TableColumnModelEvent e) { }
    public void columnMoved(TableColumnModelEvent e) { }
    ...
});
```

每个方法典型调用：`getSource()` 取得列模型 → `getFromIndex()/getToIndex()` 读取受影响的列区间。

## 3. 开源代码实际用法调研
1. `jdk25u-dev/src/java.desktop/share/classes/javax/swing/table/DefaultTableColumnModel.java:126/157/191`：
   ```java
   fireColumnAdded(new TableColumnModelEvent(this, 0, ...));
   fireColumnRemoved(new TableColumnModelEvent(this, ...));
   fireColumnMoved(new TableColumnModelEvent(this, columnIndex, newIndex));
   ```
   三种列变化分别构造事件并派发。
2. `jdk25u-dev/src/java.desktop/share/classes/javax/swing/JTable.java` 内部注册 `TableColumnModelListener` 消费该事件刷新表头。

## 4. 测试用例设计 + 自动化实现
- 白盒：保留原结构断言（继承 EventObject、public、非 abstract、无接口）。
- 黑盒：用 `DefaultTableColumnModel` 作源构造事件，验证 `getSource/getFromIndex/getToIndex` 的 round-trip；覆盖单列事件（0,0）。
- 自动化：`testWhiteBox()` + `testBlackBox()`，headless-safe。
