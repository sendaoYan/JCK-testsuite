# javax.swing.undo.AbstractUndoableEdit 设计案

## 1. API 功能
`UndoableEdit` 接口的抽象基实现，为撤销/重做提供默认行为，被 `CompoundEdit`、`StateEdit`、`UndoManager` 及 Swing 文本组件大量继承。

- **构造器**：`AbstractUndoableEdit()` —— 初始化 `hasBeenDone=true`、`alive=true`。
- **核心操作**：`undo()`（若不可撤销抛 `CannotUndoException`，置 `hasBeenDone=false`）、`redo()`（若不可重做抛 `CannotRedoException`，置 `hasBeenDone=true`）、`die()`（置 `alive=false`，单向不可复活）。
- **查询**：`canUndo()`（`alive && hasBeenDone`）、`canRedo()`（`alive && !hasBeenDone`）、`isSignificant()`（默认 `true`）。
- **组合协议**：`addEdit(UndoableEdit)`、`replaceEdit(UndoableEdit)`（默认均返回 `false`）。
- **名称**：`getPresentationName()`（默认 `""`）、`getUndoPresentationName()`/`getRedoPresentationName()`（默认 = `UIManager` 的 `AbstractUndoableEdit.undoText/redoText`，非空名称时拼接 `<文本> <name>`）。
- **工具**：`toString()`（含 `hasBeenDone`/`alive`）。

## 2. Java 用户如何使用
通常不直接 new 出裸 `AbstractUndoableEdit`，而是子类化并重写 `undo()`/`redo()`/`getPresentationName()`：

```java
AbstractUndoableEdit e = new AbstractUndoableEdit();
e.canUndo();            // true
e.undo();               // hasBeenDone -> false
e.canRedo();            // true
e.redo();               // hasBeenDone -> true
e.addEdit(other);       // false（默认不吸收）
e.die();                // alive -> false，之后 undo/redo 抛异常
```

真实组合链：`new 子类实例 → 重写 undo/redo 做业务 → super.undo()/super.redo() 维护状态 → canUndo/canRedo 判断 → die 释放`。

## 3. 开源代码实际用法调研
- `src/java.desktop/share/classes/javax/swing/text/StringContent.java`（JDK 自身实现）：`InsertUndo extends AbstractUndoableEdit`，重写 `undo()`/`redo()` 并在开头调用 `super.undo()`/`super.redo()` 维护 `hasBeenDone`，用 `string`/`posRefs` 字段做真实文本回滚，异常时抛 `CannotUndoException`/`CannotRedoException`。
- `test/jdk/javax/swing/undo/UndoManager/bug4706533.java`：`static class MyUndoableEdit extends AbstractUndoableEdit`，仅重写 `undo()`/`redo()` 空实现，作为最小可撤销单元喂给 `UndoManager`。

## 4. 测试用例设计 + 自动化实现
`AbstractUndoableEditTest.testBlackBox()` 覆盖：
- 初始状态：`canUndo`/`canRedo`/`isSignificant`/`getPresentationName==""`。
- 组合操作：`undo → canRedo → redo → canUndo` 状态翻转。
- 异常边界：双重 `undo` 抛 `CannotUndoException`、顶部 `redo` 抛 `CannotRedoException`；`die` 后 `undo`/`redo` 均抛异常。
- 协议默认值：`addEdit`/`replaceEdit` 返回 `false`。
- 名称组合：空名返回 UIManager 文本；子类非空名返回 `<undoText> <name>`。
- 白盒保留原有结构断言（父类 `Object`、实现 `UndoableEdit`+`Serializable`）。
