# javax.swing.undo.UndoManager 设计案

## 1. API 功能
`CompoundEdit` 子类并实现 `UndoableEditListener`，维护有序编辑列表与「下一个编辑」索引，是 Swing 文本组件撤销/重做栈的事实标准。

- **构造器**：`UndoManager()`（`limit=100`，`indexOfNextAdd=0`）。
- **入栈/监听**：`addEdit(UndoableEdit)`（截断已撤销尾部、去重、限流）、`undoableEditHappened(UndoableEditEvent)`。
- **撤销/重做**：`undo()`/`redo()`/`undoOrRedo()`，查询 `canUndo()`/`canRedo()`/`canUndoOrRedo()`；保护方法 `undoTo`/`redoTo`/`editToBeUndone`/`editToBeRedone`。
- **容量**：`getLimit()`/`setLimit(int)`（负值不限）、`discardAllEdits()`、`end()`。
- **名称**：`getUndoPresentationName()`/`getRedoPresentationName()`/`getUndoOrRedoPresentationName()`。
- **工具**：`toString()`。

## 2. Java 用户如何使用
```java
UndoManager manager = new UndoManager();
doc.addUndoableEditListener(manager); // 自动入栈
manager.addEdit(edit);                // 或手动入栈
manager.canUndo();                    // 是否有可撤销
manager.undo();                       // 撤销
manager.canRedo();
manager.redo();                       // 重做
manager.setLimit(100);                // 容量
manager.discardAllEdits();            // 清空
```

真实组合链（显著/非显著分组，对应类注释 `A b c D` 示例）：`addEdit(A)、addEdit(b)、addEdit(c)、addEdit(D) → undo 撤销 D → undo 撤销 c、b、A → redo 重做 A → redo 重做 b、c、D`。

## 3. 开源代码实际用法调研
- `test/jdk/javax/swing/undo/UndoManager/bug4706533.java`：`UndoManager manager = new UndoManager(); manager.setLimit(1); manager.addEdit(edit); manager.setLimit(0); try { manager.undo(); } catch (CannotUndoException e) {}`，验证 `setLimit` 裁剪语义。
- `src/java.desktop/share/classes/javax/swing/undo/UndoManager.java`（类注释）：`UndoManager undoManager = new UndoManager(); tf.getDocument().addUndoableEditListener(undoManager);`。

## 4. 测试用例设计 + 自动化实现
`UndoManagerTest.testBlackBox()` 覆盖：
- 默认状态：`getLimit==100`、`canUndo/canRedo/canUndoOrRedo==false`。
- 显著/非显著分组：`A b c D` 的 `undo`/`redo` 精确顺序断言（日志编辑）。
- `undoOrRedo`/`canUndoOrRedo` 在已撤销/已重做状态下选择正确。
- 名称：空栈回退 UIManager 文本；有编辑时指向下一个显著编辑名。
- 边界：空栈 `undo`/`redo` 抛异常；`setLimit` 裁剪并 `die` 被丢弃编辑；负 limit 不限；`discardAllEdits` 清空并 `die`；`end` 后 `addEdit` 返回 false、`setLimit` 抛 `RuntimeException`；`undoableEditHappened` 入栈；保护方法 `undoTo/redoTo/lastEdit` 经子类暴露验证。
- 白盒保留结构断言（父类 `CompoundEdit`、实现 `UndoableEditListener`）。
