# TableStringConverter 测试设计

## 1. API 功能
`javax.swing.table.TableStringConverter` 是抽象类，唯一抽象方法 `String toString(TableModel model, int row, int column)`，把表格某单元格的值转换为字符串，供 `TableRowSorter` 在无法用比较器/Comparable 时做字符串化比较。

## 2. Java 用户如何使用
- 子类化：`TableStringConverter c = new TableStringConverter() { public String toString(TableModel m, int r, int col) { Object v = m.getValueAt(r,col); return v==null?"":v.toString(); } };`
- 典型调用：`c.toString(model, 0, 0)`；`sorter.setStringConverter(c)`。
- 真实组合链路：`setStringConverter(c) → sort →（sorter 内部调用 c.toString）`。

## 3. 开源代码实际用法调研
1. JDK 实现 `src/java.desktop/share/classes/javax/swing/table/TableRowSorter.java`：`getStringValueAt` 在存在 `TableStringConverter` 时调用 `converter.toString(model, row, column)`，为 null 时回退 `""`（权威用法）。
2. SwingSet3 `test/jdk/sanity/client/lib/SwingSet3/src/com/sun/swingset3/demos/table/TableDemo.java` 通过 `TableRowSorter` 间接依赖该转换路径（过滤/排序时把非字符串值转成字符串）。

## 4. 测试用例设计 + 自动化实现
落入 `TableStringConverterTest.java` 的 `testBlackBox()`：
- 匿名子类把 `getValueAt` 结果转字符串，断言整数/字符串/null 三种输入的输出（null 自定义回退 `<null>`）。
- 使用真实 `DefaultTableModel` 作为参数传入 `toString`。
共 13 个断言。
