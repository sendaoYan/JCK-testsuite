# javax.swing.undo.StateEdit 设计案

## 1. API 功能
`AbstractUndoableEdit` 子类，通过 `StateEditable` 对象在 `Hashtable` 中快照状态，实现「撤销=恢复改前状态、重做=恢复改后状态」。

- **构造器**：`StateEdit(StateEditable)`、`StateEdit(StateEditable, String name)`（`init` 记录 `preState`）。
- **操作**：`end()`（记录 `postState` 并 `removeRedundantState()` 去掉前后相同的键）、`undo()`（`restoreState(preState)`）、`redo()`（`restoreState(postState)`）。
- **名称**：`getPresentationName()` 返回构造时传入的 `name`（可为 `null`）。
- 保护成员：`preState`/`postState`/`object`/`undoRedoName`、`init()`、`removeRedundantState()`。

## 2. Java 用户如何使用
先造编辑 → 改对象 → end → undo/redo 往返：

```java
StateEdit edit = new StateEdit(model, "rename");
model.setValue("new");   // 修改对象
edit.end();              // 快照改后状态
edit.undo();             // model 回到改前
edit.redo();             // model 回到改后
```

典型组合链：`new StateEdit(StateEditable) → storeState(pre) → 修改 → end() → storeState(post) → undo/redo 恢复`。

## 3. 开源代码实际用法调研
- `src/java.desktop/share/classes/javax/swing/undo/StateEdit.java`（类注释自带示例）：`StateEdit newEdit = new StateEdit(myObject); myObject.someStateModifyingMethod(); newEdit.end();`。
- `src/java.desktop/share/classes/javax/swing/undo/StateEdit.java` 内部实现：`end()` 构造 `postState = new Hashtable<>()` 后 `object.storeState(postState)` 再 `removeRedundantState()`。

## 4. 测试用例设计 + 自动化实现
`StateEditTest.testBlackBox()` 覆盖：
- 命名/未命名构造的 `getPresentationName`（`name` / `null`）。
- 状态往返：`store → 改 → end → undo 恢复 pre → redo 恢复 post`。
- 冗余状态移除：暴露 `preState`/`postState`，验证未变化键被移除、变化键保留且值正确。
- 白盒保留结构断言（父类 `AbstractUndoableEdit`、无接口）。
