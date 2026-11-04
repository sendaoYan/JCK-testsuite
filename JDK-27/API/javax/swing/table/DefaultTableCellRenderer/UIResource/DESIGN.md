# DefaultTableCellRenderer.UIResource 测试设计

## 1. API 功能
`javax.swing.table.DefaultTableCellRenderer.UIResource` 是 `DefaultTableCellRenderer` 的 public static 嵌套类，额外实现 `javax.swing.plaf.UIResource` 标记接口，表示该渲染器实例来自 Look&Feel（可被 UI 更新替换），行为与父类一致。

## 2. Java 用户如何使用
- 构造：`DefaultTableCellRenderer.UIResource r = new DefaultTableCellRenderer.UIResource();`
- 每个方法典型调用：`r instanceof UIResource`；`r.getTableCellRendererComponent(new JTable(), "v", false, false, 0, 0)`；`r.getText()`；`r.setText(...)`；`r.setForeground(color)`。
- 真实组合链路：`new UIResource() → getTableCellRendererComponent → getText()`。

## 3. 开源代码实际用法调研
1. JDK 实现 `src/java.desktop/share/classes/javax/swing/table/DefaultTableCellRenderer.java`：`public static class UIResource extends DefaultTableCellRenderer implements UIResource`，仅添加标记接口。
2. 该标记被 Swing L&F（如 `BasicTableUI`/`MetalLookAndFeel`）用于区分"系统默认"与"用户自定义"渲染器，安装时返回 UIResource 实例。

## 4. 测试用例设计 + 自动化实现
落入 `UIResourceTest.java` 的 `testBlackBox()`：
- 断言实例是 `DefaultTableCellRenderer` 与 `UIResource`。
- 渲染返回自身、设置文本、`setText/getText`、`set/getForeground`。
共 16 个断言。
