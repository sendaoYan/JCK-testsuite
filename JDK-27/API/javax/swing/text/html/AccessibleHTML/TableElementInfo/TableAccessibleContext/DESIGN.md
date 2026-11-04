# javax.swing.text.html.AccessibleHTML.TableElementInfo.TableAccessibleContext — DESIGN

## 1. API 功能
`public class TableAccessibleContext extends HTMLAccessibleContext implements AccessibleTable, AccessibleExtendedTable`，表示 `<table>` 元素的可访问信息。关键方法：
- `getAccessibleRole()` 返回 `AccessibleRole.TABLE`，`getAccessibleName()` 返回 `"table"`。
- `getAccessibleRowCount()/getAccessibleColumnCount()`：网格行列数。
- `getAccessibleAt(r,c)`：返回单元格内最外层 Accessible；越界返回 null。
- `getAccessibleRowExtentAt/ColumnExtentAt`：单元格 rowspan/colspan。
- `getAccessibleCaption/Summary/RowHeader/ColumnHeader`：无对应元素时 null。
- 选中相关（`isAccessibleSelected/isAccessibleRowSelected/isAccessibleColumnSelected/getSelectedAccessibleRows/Columns`）。

## 2. Java 用户如何使用
由 `AccessibleHTML.createElementInfo` 对 TABLE 创建 `TableElementInfo`，其 `getAccessibleContext()` 惰性创建本类。测试通过可访问树递归遍历（findContext 后缀 "TableAccessibleContext"）获取。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/AccessibleHTML.java`：`class TableElementInfo`（第 1414 行附近）+ `class TableAccessibleContext`（第 1584 行附近）。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/accessibility/AccessibleTable.java`：接口定义。

## 4. 测试用例设计（黑盒部分，`TableAccessibleContextTest.testBlackBox`）
1. findContext 得到 TableAccessibleContext、instanceof、`instanceof AccessibleTable`。
2. 角色 TABLE、name `"table"`。
3. 2x2 表：row/col count == 2、children == 4。
4. `getAccessibleAt(0,0)/(1,1)` 非 null、`(2,2)` null；extent == 1。
5. caption/summary/rowHeader/columnHeader null；setter 可调用。
6. 选中状态全 false、selected 数组长度 0。
