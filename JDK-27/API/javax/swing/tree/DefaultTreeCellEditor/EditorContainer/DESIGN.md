# javax.swing.tree.DefaultTreeCellEditor.EditorContainer 行为化测试设计

## 1. API 功能
`DefaultTreeCellEditor` 的内部类，继承 `java.awt.Container`，承载正在编辑的组件。构造后无子组件、布局为 null；`getPreferredSize()` 在无编辑组件时返回 (0,0)；`doLayout()` 在无编辑组件时为 no-op；`paint(Graphics)` 可安全绘制。

## 2. Java 用户如何使用
`DefaultTreeCellEditor` 内部维护 `editingContainer`，编辑开始时 `editingContainer.removeAll(); editingContainer.add(...);`，结束编辑时清空。用户一般不直接实例化。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/tree/DefaultTreeCellEditor.java`：构造器内 `editingContainer = createContainer();`，`createContainer()` 返回 `new EditorContainer()`；`addCellEditorListener` 的 `editingStopped` 回调中 `editingContainer.removeAll()`。
- 该类仅供内部使用，未发现公开外部直接实例化样例。

## 4. 测试用例设计 + 自动化实现
黑盒部分在 `EditorContainerTest.java`：
- `new DefaultTreeCellEditor(...).new EditorContainer()` 构造，`getComponentCount()==0`、`getLayout()==null`。
- `getPreferredSize()` 为 (0,0)；`setSize` 后 `doLayout()` 不抛。
- 在 `BufferedImage` 的 `Graphics` 上 `paint(g)` 成功（headless-safe）。
