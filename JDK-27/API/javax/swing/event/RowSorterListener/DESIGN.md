# javax.swing.event.RowSorterListener 测试设计

## 1. API 功能

`RowSorterListener` 监听 `javax.swing.RowSorter` 排序/过滤变化，继承 `java.util.EventListener`，一个回调：

- `void sorterChanged(RowSorterEvent e)`：排序顺序变化或行被重新排序时触发。

事件 `RowSorterEvent` 携带：`getSource()`（返回 `RowSorter<?>`）、`getType()`（`Type.SORT_ORDER_CHANGED` / `Type.SORTED`）、`getPreviousRowCount()`、`convertPreviousRowIndexToModel(int)`。

## 2. Java 用户如何使用

构造：实现接口并注册到 `RowSorter`：

```java
TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
RowSorterListener l = new RowSorterListener() {
    public void sorterChanged(RowSorterEvent e) { /* 排序变化 */ }
};
sorter.addRowSorterListener(l);
sorter.toggleSortOrder(0);   // 触发 SORT_ORDER_CHANGED 与后续 SORTED
```

事件对象由 `RowSorter.fireSortOrderChanged()/fireRowSorterChanged(int[])` 构造。测试时可手工 `new RowSorterEvent(sorter, Type.SORTED, prevMapping)` 或单参 `new RowSorterEvent(sorter)`。

典型组合链路：`addRowSorterListener → setSortKeys → SORT_ORDER_CHANGED → 重排 → SORTED（带 previousRowIndexToModel）→ convertPreviousRowIndexToModel(viewIndex) 反查旧模型行`。

## 3. 开源代码实际用法调研

1. JDK `RowSorter.addRowSorterListener`（`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/RowSorter.java:304`）注册监听器。
2. `RowSorter.fireSortOrderChanged()`（同文件 `:321`）内部 `fireRowSorterChanged(new RowSorterEvent(this))`；`fireRowSorterChanged(int[])`（`:331`）带映射构造 `SORTED` 事件，是两种 `Type` 的真实触发源。
3. `JTable` 与 `sun.swing.FilePane` 都引用 `RowSorterListener`（`~/git/jdk25u-dev/src/java.desktop/share/classes/sun/swing/FilePane.java`），用于排序变化后刷新。

## 4. 测试用例设计 + 自动化实现

黑盒设计（`RowSorterListenerTest.testBlackBox()`）：
- 匿名实现记录事件。
- 用 `TableRowSorter` 作 source，构造 `SORTED` 事件（映射 `{5,3,1}`）与单参 `SORT_ORDER_CHANGED` 事件，分别调用 `sorterChanged`。
- 断言 `getSource()==sorter`（typed `RowSorter`）、`getType()`、`getPreviousRowCount()`、`convertPreviousRowIndexToModel`（含越界/负索引返回 -1）。
- 断言 `Type.values().length==2`、`valueOf` 映射正确。

白盒保留原有结构断言，新增 `sorterChanged(RowSorterEvent)` 返回 void/抽象与 `declared method count == 1`。
