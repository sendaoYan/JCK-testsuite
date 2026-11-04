# javax.swing.text.AbstractDocument.ElementEdit — DESIGN

## 1. API 功能
`AbstractUndoableEdit` 子类并实现 `DocumentEvent.ElementChange`，记录元素结构的一次变更（某个 `BranchElement` 在指定下标删除/新增子元素）。方法分组：
- 查询：`getElement()`、`getIndex()`、`getChildrenRemoved()`、`getChildrenAdded()`。
- 撤销/重做：`undo()`、`redo()`（交换 added/removed 并调用 `BranchElement.replace`）。

## 2. Java 用户如何使用
static 嵌套类，可直接 `new AbstractDocument.ElementEdit(elem, index, removed, added)` 构造记录；`undo()` 恢复被删除子元素，`redo()` 重新应用新增子元素。通常在文档结构变更时由 `PlainDocument`/`DefaultStyledDocument` 内部创建。

## 3. 开源代码实际用法调研（来源：jdk8u 源码 + jdk/test）
- `jdk/src/share/classes/javax/swing/text/PlainDocument.java:222`：`ElementEdit ee = new ElementEdit(lineMap, index, relems, aelems); chng.addEdit(ee);`。
- `jdk/src/share/classes/javax/swing/text/PlainDocument.java:262`：`removeUpdate` 里同样 `new ElementEdit(map, line0, relems, aelems)`。
- `jdk/src/share/classes/javax/swing/text/AbstractDocument.java:3136`：构造函数与 `redo/undo` 的 added/removed 交换实现。

## 4. 测试用例设计 + 自动化实现
`testGettersAndUndoRedo/testFromEvent`（33 断言）：
- 公有构造器（public）直接可用；getElement/getIndex/getChildrenRemoved/getChildrenAdded 回读精确；实现 DocumentEvent.ElementChange 且是 UndoableEdit、significant。
- 状态机反例：新编辑处于 done 态只能 undo（直接 redo 抛 CannotRedoException）；undo→redo→undo 循环中 added/removed 元素精确装回/还原，未涉及的兄弟不受影响；空记录容错。
- 从真实 DocumentEvent 提取的 ElementEdit：元素/索引/added/removed 与结构变化一致。
