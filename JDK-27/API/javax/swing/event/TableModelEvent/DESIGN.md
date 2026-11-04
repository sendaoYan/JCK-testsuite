# TableModelEvent 行为测试设计

## 1. API 功能
`javax.swing.event.TableModelEvent` 是表格模型变更事件，继承 `java.util.EventObject`。

- **常量**：`INSERT = 1`、`UPDATE = 0`、`DELETE = -1`、`HEADER_ROW = -1`、`ALL_COLUMNS = -1`。
- **构造器**：五组重载 `(TableModel)`、`(TableModel,int row)`、`(TableModel,int firstRow,int lastRow)`、`(TableModel,int firstRow,int lastRow,int column)`、`(TableModel,int firstRow,int lastRow,int column,int type)`；缺省参数默认 `column=ALL_COLUMNS`、`type=UPDATE`，单参构造器默认 `firstRow=0,lastRow=Integer.MAX_VALUE`。
- **查询**：`getFirstRow()`、`getLastRow()`、`getColumn()`、`getType()`。

## 2. Java 用户如何使用
`TableModel` 实现派发，用户实现 `TableModelListener`：

```java
model.addTableModelListener(e -> {
    int type = e.getType();                 // INSERT/UPDATE/DELETE
    int first = e.getFirstRow(), last = e.getLastRow();
    int col = e.getColumn();                // ALL_COLUMNS 表示整行
});
```

每个方法典型调用：`getType()` 判断变化类别 → `getFirstRow()/getLastRow()` 定位行区间 → `getColumn()` 定位列（或 ALL_COLUMNS）。

## 3. 开源代码实际用法调研
1. `jdk25u-dev/src/java.desktop/share/classes/javax/swing/JTable.java:3774`：
   ```java
   tableChanged(new TableModelEvent(dataModel, TableModelEvent.HEADER_ROW));
   ```
   用单行构造器 + `HEADER_ROW` 通知表头结构变化。
2. `jdk25u-dev/src/jdk.jconsole/.../XMBeanAttributes.java` 等内部工具消费 `TableModelEvent` 刷新表格视图。

## 4. 测试用例设计 + 自动化实现
- 白盒：保留原结构断言（继承 EventObject、public、非 abstract、无接口）。
- 黑盒：断言五个常量值；覆盖五组构造器的默认参数语义（单参 → 0..Integer.MAX_VALUE/ALL_COLUMNS/UPDATE；两/三/四参 → 缺省列与类型）；五参构造器验证 INSERT/DELETE 类型与 HEADER_ROW 边界。
- 自动化：`testWhiteBox()` + `testBlackBox()`，headless-safe。
