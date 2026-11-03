# javax.swing.text.html.AccessibleHTML.TableElementInfo.TableAccessibleContext.AccessibleHeadersTable — DESIGN

## 1. API 功能
`protected class AccessibleHeadersTable implements AccessibleTable`（直接继承 Object），表示 `<th>` 表头单元格组成的行/列头只读视图。内部用 `Hashtable<Integer, ArrayList<TableCellElementInfo>>` 按行号存表头单元格。关键行为：
- `addHeader(cellInfo, rowNumber)`：在 `TableRowElementInfo.loadChildren` 发现 TH 时被调用。
- `getAccessibleRowCount()/getAccessibleColumnCount()`：返回字段 `rowCount/columnCount`（实现中始终为 0）。
- `getAccessibleAt(r,c)`：表头单元格是 `TableCellElementInfo`（非 Accessible）→ 恒返回 null。
- `getAccessibleRowExtentAt/ColumnExtentAt`：返回表头单元格 rowspan/colspan（无则 0）。
- caption/summary/rowHeader/columnHeader 均 null；选中状态全 false。

## 2. Java 用户如何使用
不直接实例化。用户先触发主表校验（如 `getAccessibleRowCount()`），使 TH 单元格加载进 `rowHeadersTable`，再调用 `TableAccessibleContext.getAccessibleRowHeader()` 取得本类实例。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/AccessibleHTML.java`：`class AccessibleHeadersTable`（第 2136 行附近）；`TableRowElementInfo.loadChildren`（第 2416 行附近）调用 `tableElement.addRowHeader(...)`。

## 4. 测试用例设计（黑盒部分，`AccessibleHeadersTableTest.testBlackBox`）
1. 含 `<th>` 的表格，先 `getAccessibleRowCount()` 触发校验，再 `getAccessibleRowHeader()` 非 null。
2. `instanceof AccessibleHeadersTable`。
3. `getAccessibleRowCount()==0`、`getAccessibleColumnCount()==0`（字段未累加）。
4. `getAccessibleAt(0,0)==null`（单元格非 Accessible）。
5. extent(0,0)==1、extent(5,5)==0；caption/summary/rowHeader/columnHeader null。
6. setter 可调用；选中状态全 false、selected 数组长度 0。
