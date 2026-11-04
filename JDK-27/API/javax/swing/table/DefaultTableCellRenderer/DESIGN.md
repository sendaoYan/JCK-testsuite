# DefaultTableCellRenderer 测试设计

## 1. API 功能
`javax.swing.table.DefaultTableCellRenderer` 继承 `JLabel` 并实现 `TableCellRenderer`/`Serializable`，是单元格的默认渲染器。核心分组：

- **渲染**：`getTableCellRendererComponent(JTable, value, isSelected, hasFocus, row, column)` 返回自身并设置文本/前景/背景/字体/边框。
- **颜色**：`set/getForeground`、`set/getBackground`（继承自 JComponent/JLabel）。
- **UI/状态**：`updateUI`、`isOpaque`、`invalidate/validate/revalidate`、`repaint`。
- **文本**：JLabel 的 `setText/getText`（内部 `setValue` 用 `value.toString()`）。

## 2. Java 用户如何使用
- 构造：`DefaultTableCellRenderer r = new DefaultTableCellRenderer();`
- 每个方法典型调用：`r.setForeground(Color.RED)`；`r.setBackground(Color.BLUE)`；`r.getTableCellRendererComponent(new JTable(), "Hello", false, false, 0, 0)`；`r.getText()`；`r.setText(...)`；`r.updateUI()`；`r.isOpaque()`。
- 真实组合链路：`getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col) → 返回自身 → getText()`。

## 3. 开源代码实际用法调研
1. `test/jdk/javax/swing/DefaultTableCellRenderer/bug4240870.java` 直接针对该类的渲染/文本行为。
2. `test/jdk/javax/swing/JTable/*` 系列（`KeyBoardNavigation.java`、`InternationalCharacters.java` 等）通过 `getTableCellRendererComponent` 触发单元格渲染并断言组件文本。
3. JDK 实现 `src/java.desktop/share/classes/javax/swing/table/DefaultTableCellRenderer.java`：`getTableCellRendererComponent` 在 `table==null` 时直接 `return this`（短路），否则设置前景/背景后 `setValue(value)` 设文本。

## 4. 测试用例设计 + 自动化实现
落入 `DefaultTableCellRendererTest.java` 的 `testBlackBox()`：
- 构造 + `isOpaque`；`set/getForeground`、`set/getBackground` 往返。
- `getTableCellRendererComponent` 返回自身，并分别对 String/Integer/null/selected 断言 `getText`。
- 以 `TableCellRenderer` 接口视角调用仍返回同一组件；`setText/getText`；`updateUI` 可调用；`null` 表短路返回自身不抛异常。
共 25 个断言。
