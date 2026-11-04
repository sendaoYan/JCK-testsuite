# javax.swing.undo.CompoundEdit 设计案

## 1. API 功能
`AbstractUndoableEdit` 子类，把一组子编辑聚合为一个整体撤销/重做单位（`UndoManager` 的父类）。

- **构造器**：`CompoundEdit()` —— `inProgress=true`，`edits` 为空 `Vector`。
- **组合**：`addEdit(UndoableEdit)`（inProgress 时吸收子编辑，先让末子编辑 `addEdit`、再让其 `replaceEdit` 机会）；`end()`（置 `inProgress=false`）。
- **操作**：`undo()`（逆序撤销子编辑）、`redo()`（正序重做子编辑）、`die()`（逆序 `die` 子编辑再 `super.die()`）。
- **查询**：`canUndo()`/`canRedo()`（`!inProgress && super.*`）、`isInProgress()`、`isSignificant()`（任一子编辑显著即显著）。
- **名称**：`getPresentationName()`/`getUndoPresentationName()`/`getRedoPresentationName()` 取末子编辑的对应名，空时退回 `super`。
- **工具**：`toString()`。

## 2. Java 用户如何使用
`new CompoundEdit() → addEdit(子编辑…) → end() → undo()/redo() → canUndo/canRedo`：

```java
CompoundEdit ce = new CompoundEdit();
ce.addEdit(editA);   // inProgress 期间返回 true
ce.addEdit(editB);
ce.end();            // 结束聚合
ce.canUndo();        // true
ce.undo();           // 逆序 undo editB、editA
ce.redo();           // 正序 redo editA、editB
ce.addEdit(editC);   // end 之后返回 false
```

## 3. 开源代码实际用法调研
- `src/java.desktop/share/classes/javax/swing/undo/UndoableEditSupport.java`：`beginUpdate()` 内 `createCompoundEdit()` 返回 `new CompoundEdit()`，`postEdit` 期间把子编辑 `compoundEdit.addEdit(e)`，`endUpdate()` 时 `compoundEdit.end()` 后一次性广播——是 CompoundEdit 的典型聚合用法。
- `src/java.desktop/share/classes/javax/swing/undo/UndoManager.java`：`UndoManager extends CompoundEdit`，复用其 `edits` 向量与 `end()` 语义。

## 4. 测试用例设计 + 自动化实现
`CompoundEditTest.testBlackBox()` 覆盖：
- 初始 `inProgress`、`canUndo/canRedo=false`、空 `isSignificant=false`。
- `addEdit` 组合 + `isSignificant`/`getPresentationName` 取末编辑。
- `end` 后 `undo`（逆序）/`redo`（正序）子编辑顺序（用日志编辑验证 `[undo:e2, undo:e1]`）。
- 边界：`end` 后 `addEdit` 返回 `false`；双重 `undo` 抛 `CannotUndoException`、顶部 `redo` 抛 `CannotRedoException`；`die` 传播到子编辑；空聚合 `getPresentationName==""`。
- 白盒保留结构断言（父类 `AbstractUndoableEdit`、无接口）。
