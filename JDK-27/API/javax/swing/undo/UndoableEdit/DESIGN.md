# javax.swing.undo.UndoableEdit 设计案

## 1. API 功能
撤销/重做编辑的核心接口，定义单个可撤销操作的完整协议（11 个方法），`AbstractUndoableEdit` 是其默认实现。

- **操作**：`undo()`（可抛 `CannotUndoException`）、`redo()`（可抛 `CannotRedoException`）、`die()`。
- **查询**：`canUndo()`、`canRedo()`、`isSignificant()`。
- **组合协议**：`addEdit(UndoableEdit)`、`replaceEdit(UndoableEdit)`。
- **名称**：`getPresentationName()`、`getUndoPresentationName()`、`getRedoPresentationName()`。

## 2. Java 用户如何使用
实现接口（或继承 `AbstractUndoableEdit`）后通过接口引用操作：

```java
UndoableEdit e = new MyEdit();
e.canUndo(); e.undo(); e.canRedo(); e.redo();
e.isSignificant(); e.getPresentationName();
e.addEdit(other); e.replaceEdit(other); e.die();
```

典型组合链：`new 实现 → undo 翻转状态 → canRedo → redo 翻转回 → canUndo`。

## 3. 开源代码实际用法调研
- `src/java.desktop/share/classes/javax/swing/undo/AbstractUndoableEdit.java`：`public class AbstractUndoableEdit implements UndoableEdit, Serializable`，为接口提供默认实现。
- `src/java.desktop/share/classes/javax/swing/text/DefaultStyledDocument.java`：`AttributeUndoableEdit extends AbstractUndoableEdit`（间接实现该接口），用于属性变更撤销。

## 4. 测试用例设计 + 自动化实现
`UndoableEditTest.testBlackBox()` 覆盖：
- 最小实现 `SimpleEdit` 经接口引用的 `undo/redo/canUndo/canRedo` 状态翻转。
- `isSignificant`/三个名称方法返回实现值。
- `addEdit`/`replaceEdit` 返回 `false`、`die()` 可调用。
- 白盒保留结构断言（接口、无父类、11 个方法），并抽查 `undo/redo/die/getPresentationName` 存在。
