# TableCellRenderer 测试设计

## 1. API 功能
`javax.swing.table.TableCellRenderer` 是函数式接口，唯一抽象方法 `Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)`，返回用于绘制单元格的组件。

## 2. Java 用户如何使用
- 实现：`TableCellRenderer r = (table, value, isSelected, hasFocus, row, column) -> { JLabel l = new JLabel(); l.setText(value==null?"":value.toString()); return l; };`
- 典型调用：`r.getTableCellRendererComponent(new JTable(), "Hello", false, false, 0, 0)`。
- 真实组合链路：`getTableCellRendererComponent → 返回 Component → (JLabel)getText()`。

## 3. 开源代码实际用法调研
1. JDK 实现 `src/java.desktop/share/classes/javax/swing/table/DefaultTableCellRenderer.java`：实现该接口，`getTableCellRendererComponent` 返回 `this` 并设置文本。
2. SwingSet3 `test/jdk/sanity/client/lib/SwingSet3/src/com/sun/swingset3/demos/table/TableDemo.java` 自定义 `OscarCellRenderers.RowRenderer` 等实现 `TableCellRenderer` 返回不同渲染组件。

## 4. 测试用例设计 + 自动化实现
落入 `TableCellRendererTest.java` 的 `testBlackBox()`：
- 匿名 lambda 实现，断言返回 Component 且为 JLabel、文本来自 value。
- Integer 与 null 输入的文本转换。
共 13 个断言。
