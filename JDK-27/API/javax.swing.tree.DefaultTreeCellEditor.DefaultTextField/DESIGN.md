# javax.swing.tree.DefaultTreeCellEditor.DefaultTextField 行为化测试设计

## 1. API 功能
`DefaultTreeCellEditor` 的内部类，继承 `JTextField`，作为缺省内联编辑组件。构造器 `DefaultTextField(Border)` 保存传入边框，`getBorder()` 返回该边框、`setBorder(Border)` 覆盖为记忆该边框；`getPreferredSize()` 返回默认文本框尺寸。

## 2. Java 用户如何使用
`DefaultTreeCellEditor#createTreeCellEditor()` 内部用 `new DefaultCellEditor(new DefaultTextField(null))` 创建真实编辑器；用户一般不直接实例化，而是通过父类缺省构造间接创建。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/tree/DefaultTreeCellEditor.java`：`createTreeCellEditor()` 返回 `new DefaultCellEditor(new DefaultTextField(null))`，随后 `((DefaultTextField)getComponent()).setBorder(border)`。
- 该类仅供 `DefaultTreeCellEditor` 内部使用，未在公开 API 或 test/jdk 中发现直接实例化样例。

## 4. 测试用例设计 + 自动化实现
黑盒部分在 `DefaultTextFieldTest.java`：
- 通过 `new DefaultTreeCellEditor(...).new DefaultTextField(border)` 构造（实例化内部类）。
- `getBorder()` 返回构造传入的 `EmptyBorder`；`setBorder(border2)/getBorder()` 往返。
- `getFont()` 非空；`getPreferredSize()` 宽高非负；继承的 `JTextField` 行为 `setText/getText` 正常。
