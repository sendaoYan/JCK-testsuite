# JTableHeader.AccessibleJTableHeader.AccessibleJTableHeaderEntry 测试设计

## 1. API 功能
`javax.swing.table.JTableHeader.AccessibleJTableHeader.AccessibleJTableHeaderEntry` 是 protected 嵌套类，继承 `AccessibleContext` 并实现 `Accessible`/`AccessibleComponent`，表示单个列头单元格的无障碍对象。核心方法：

- **上下文**：`getAccessibleContext()` 返回自身；`getAccessibleIndexInParent()` 返回列索引。
- **名称/角色**：`get/setAccessibleName`、`get/setAccessibleDescription`、`getAccessibleRole`、`getAccessibleStateSet`。
- **子对象/组件**：`getAccessibleChildrenCount`、`getAccessibleChild`、`getAccessibleComponent`、`getAccessibleAction/Selection/Text/Value`。
- **AccessibleComponent 方法**：`get/setBackground/Foreground/Font/Cursor/Enabled/Visible/Bounds/Location/Size`、`getLocationOnScreen`、`contains`、`isShowing`、`isFocusTraversable`、`requestFocus`、`add/removeFocusListener` 等。

## 2. Java 用户如何使用
- 获取实例：`Accessible child = header.getAccessibleContext().getAccessibleChild(0);`
- 每个方法典型调用：`child.getAccessibleContext()`；`entry.getAccessibleIndexInParent()`；`entry.getAccessibleName()`；`entry.setAccessibleName("x")`；`entry.getAccessibleRole()`。
- 真实组合链路：`getAccessibleContext().getAccessibleChild(0) → 强转 AccessibleContext → getAccessibleIndexInParent/setAccessibleName/getAccessibleName`。

## 3. 开源代码实际用法调研
1. JDK 实现 `src/java.desktop/share/classes/javax/swing/table/JTableHeader.java`：`AccessibleJTableHeaderEntry` 构造器记录 `column` 字段，`getAccessibleIndexInParent()` 返回 `column`；`getAccessibleName()` 优先取列头渲染器的无障碍名称，回退到 `table.getColumnName(column)`；`getAccessibleContext()` 返回 `this`（本测试断言依据）。
2. `test/jdk/javax/swing/JTableHeader/bug4243927.java` 覆盖该条目相关 NPE 回归。

## 4. 测试用例设计 + 自动化实现
落入 `AccessibleJTableHeaderEntryTest.java` 的 `testBlackBox()`：
- 用真实 `JTable` + `JTableHeader`（设列 0 的 `headerValue="IDCol"`）取 `getAccessibleChild(0)`。
- 断言 `getAccessibleContext()` 返回自身、`getAccessibleIndexInParent()==0`。
- `getAccessibleName()` 等于列头值；`setAccessibleName("CustomHeader")`/`getAccessibleName()` 往返；角色非空；子数量非负。
- 列 1 条目 `getAccessibleIndexInParent()==1`。
共 20 个断言。
