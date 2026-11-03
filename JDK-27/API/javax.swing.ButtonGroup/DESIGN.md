# javax.swing.ButtonGroup 测试设计

## 1. API 功能
- 纯模型（headless），管理一组 `AbstractButton` 的单选互斥。
- `add(AbstractButton)`、`remove(AbstractButton)`、`getButtonCount()`、`getElements()`。
- 选择：`setSelected(ButtonModel, boolean)`、`isSelected(ButtonModel)`、`getSelection()`、`clearSelection()`。

## 2. Java 用户如何使用
典型组合：`new ButtonGroup()` → `add(rb1)`/`add(rb2)` → `setSelected(rb1.getModel(), true)` 单选，切换 `setSelected(rb2.getModel(), true)` 时 rb1 自动取消；`clearSelection()` 清空；`getSelection()`/`isSelected(model)` 查询当前选中。

## 3. 开源代码实际用法调研
- JDK 自带测试 `test/jdk/javax/swing/JRadioButton/8033699/bug8033699.java` 与 `JMenuItem/TestRadioAndCheckMenuItemWithIcon.java`：用 ButtonGroup 组织单选按钮/菜单项并验证互斥。
- JDK 源码 `javax.swing.ButtonGroup.add`：预选中按钮加入时若已有 selection 则 `setSelected(false)`，是互斥语义的权威实现。
- 常见表单（性别/单选选项）用 ButtonGroup + JRadioButton 实现单选。

## 4. 测试用例设计 + 自动化实现
黑盒覆盖：初始 count/selection/elements 状态；add 与 count/elements；setSelected 切换时的互斥链路（选中一个自动取消另一个，`isSelected`/`getSelection`/按钮 `isSelected` 同步）；`setSelected(false)` 无副作用；`clearSelection` 清空；remove（含重复 remove、null add/remove 无副作用）；预选中按钮加入的 selection 语义；移除选中按钮后 selection 清空。见 `ButtonGroupTest.java` 的 `testBlackBox()`。
