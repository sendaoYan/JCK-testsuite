# javax.swing.undo.UndoableEditSupport 设计案

## 1. API 功能
把 `UndoableEdit` 分发给一组 `UndoableEditListener` 的广播器，支持 `beginUpdate`/`endUpdate` 把多个编辑聚合为一个 `CompoundEdit` 后一次性广播。

- **构造器**：`UndoableEditSupport()`（源为 `this`）、`UndoableEditSupport(Object realSource)`。
- **监听器**：`addUndoableEditListener`/`removeUndoableEditListener`/`getUndoableEditListeners()`。
- **分发**：`postEdit(UndoableEdit)`、保护方法 `_postEdit`、`createCompoundEdit()`。
- **聚合**：`beginUpdate()`/`endUpdate()`/`getUpdateLevel()`。
- **工具**：`toString()`。

## 2. Java 用户如何使用
```java
UndoableEditSupport support = new UndoableEditSupport(source);
support.addUndoableEditListener(listener);
support.postEdit(edit);       // 立即通知
support.beginUpdate();        // 开始聚合
support.postEdit(e1);
support.postEdit(e2);
support.endUpdate();          // 一次性广播 CompoundEdit
```

典型组合链：`new(源) → addUndoableEditListener → postEdit（即时）→ beginUpdate → postEdit × n → endUpdate（聚合广播）→ removeUndoableEditListener`。

## 3. 开源代码实际用法调研
- `src/java.desktop/share/classes/javax/swing/undo/UndoableEditSupport.java`（自身实现）：`beginUpdate()` 在 `updateLevel==0` 时 `compoundEdit = createCompoundEdit()`；`postEdit` 在 `updateLevel!=0` 时 `compoundEdit.addEdit(e)`；`endUpdate()` 归零时 `compoundEdit.end(); _postEdit(compoundEdit); compoundEdit = null;`。
- `src/java.desktop/share/classes/javax/swing/undo/UndoableEditSupport.java`：`_postEdit` 对 `listeners.clone()` 逐个 `undoableEditHappened(ev)`，`ev = new UndoableEditEvent(realSource, e)`。

## 4. 测试用例设计 + 自动化实现
`UndoableEditSupportTest.testBlackBox()` 覆盖：
- `getUpdateLevel` 初始 0；`addUndoableEditListener`/`getUndoableEditListeners` 计数与快照拷贝。
- `postEdit` 立即通知且事件源为 `realSource`；`removeUndoableEditListener` 停止通知。
- `beginUpdate/endUpdate` 聚合：期间不通知、`endUpdate` 一次性广播 `CompoundEdit`（其 `getPresentationName` 取末子编辑名）。
- 嵌套 `beginUpdate` 归零才广播；默认构造源为自身。
- 白盒保留结构断言（父类 `Object`、无接口）。
