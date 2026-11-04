# JTableHeader.AccessibleJTableHeader 测试设计

## 1. API 功能
`javax.swing.table.JTableHeader.AccessibleJTableHeader` 是 `JTableHeader` 的 protected 嵌套类，继承 `JComponent.AccessibleJComponent`，实现列头的无障碍访问。核心方法：

- **角色**：`getAccessibleRole()` 返回 `AccessibleRole.PANEL`。
- **子对象**：`getAccessibleChildrenCount()` 返回列数；`getAccessibleChild(int)` 返回对应列的 `AccessibleJTableHeaderEntry`（越界返回 null）。
- **命中**：`getAccessibleAt(Point)` 按坐标返回所在列的条目，无命中返回 null。

## 2. Java 用户如何使用
- 获取实例：`AccessibleContext ac = header.getAccessibleContext();`（内部 `new AccessibleJTableHeader()`）。
- 每个方法典型调用：`ac.getAccessibleRole()`；`ac.getAccessibleChildrenCount()`；`ac.getAccessibleChild(i)`；`ac.getAccessibleAt(p)`。
- 真实组合链路：`header.getAccessibleContext() → getAccessibleChildrenCount() → getAccessibleChild(i)`。

## 3. 开源代码实际用法调研
1. `test/jdk/javax/swing/JTableHeader/bug4243927.java`：专门针对 `JTableHeader.AccessibleJTableHeader` 不抛 NPE 的回归用例，构造 `JTableHeader` 后加列并访问无障碍上下文（本测试同款场景）。
2. JDK 实现 `src/java.desktop/share/classes/javax/swing/table/JTableHeader.java`：`getAccessibleContext()` 惰性 `new AccessibleJTableHeader()`；`getAccessibleChildrenCount()` 直接返回 `columnModel.getColumnCount()`；`getAccessibleChild(i)` 越界返回 null（本测试断言依据）。

## 4. 测试用例设计 + 自动化实现
落入 `AccessibleJTableHeaderTest.java` 的 `testBlackBox()`：
- 3 列列模型 → `getAccessibleContext()` 非空、角色 PANEL、子数量==列数。
- `getAccessibleChild(0/2)` 非空、`getAccessibleChild(-1/3)` 为 null。
- `getAccessibleAt(new Point(-5,0))`（反射调用具体方法）返回 null。
共 17 个断言。
