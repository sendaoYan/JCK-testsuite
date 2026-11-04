# RowSorterEvent 行为测试设计

## 1. API 功能
`javax.swing.event.RowSorterEvent` 是行排序器排序/排序顺序变化事件，继承 `java.util.EventObject`。

- **构造器**：`RowSorterEvent(RowSorter<?> source)`（类型固定为 SORT_ORDER_CHANGED，映射为 null）与 `RowSorterEvent(RowSorter<?> source, Type type, int[] previousRowIndexToModel)`（type 为 null 抛 `IllegalArgumentException`）。
- **查询**：`getType()`、`getSource()`（协变返回 `RowSorter<?>`）、`getPreviousRowCount()`（映射为 null 时为 0，否则为数组长度）、`convertPreviousRowIndexToModel(int)`（越界/无映射返回 -1）。

## 2. Java 用户如何使用
`RowSorter` 子类在排序变化时 `fireSortOrderChanged()`/`fireRowSorterChanged(int[])`，用户实现 `RowSorterListener`：

```java
sorter.addRowSorterListener(e -> {
    if (e.getType() == RowSorterEvent.Type.SORTED) {
        int prev = e.convertPreviousRowIndexToModel(viewIndex); // 排序前映射
        int count = e.getPreviousRowCount();
    }
});
```

每个方法典型调用：`getType()` 判断 SORTED/SORT_ORDER_CHANGED → `getPreviousRowCount()` 获取旧映射长度 → `convertPreviousRowIndexToModel(i)` 查询旧模型行号。

## 3. 开源代码实际用法调研
1. `jdk25u-dev/src/java.desktop/share/classes/javax/swing/RowSorter.java:322/332`：
   ```java
   fireRowSorterChanged(new RowSorterEvent(this));
   fireRowSorterChanged(new RowSorterEvent(this, RowSorterEvent.Type.SORTED, lastRowIndexToModel));
   ```
   两个受保护的通知方法分别对应两种构造器。
2. `jdk25u-dev/src/java.desktop/share/classes/javax/swing/event/RowSorterListener.java` 定义了消费该事件的监听接口。

## 4. 测试用例设计 + 自动化实现
- 白盒：保留原结构断言（继承 EventObject、public、非 abstract、无接口）。
- 黑盒：用最小 `RowSorter` 子类，验证单参构造器得到 SORT_ORDER_CHANGED/prevCount 0/convert 返回 -1；三参构造器得到 SORTED/prevCount 3/convert 各索引映射与越界 -1；type=null 抛 `IllegalArgumentException`。
- 自动化：`testWhiteBox()` + `testBlackBox()`，headless-safe。
