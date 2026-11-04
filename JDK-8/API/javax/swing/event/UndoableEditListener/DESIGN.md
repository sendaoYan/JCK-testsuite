# javax.swing.event.UndoableEditListener 测试设计

## 1. API 功能

`UndoableEditListener` 监听可撤销编辑（`javax.swing.undo.UndoableEdit`）的产生，继承 `java.util.EventListener`，一个回调：

- `void undoableEditHappened(UndoableEditEvent e)`：发生一个可撤销编辑时触发。

事件 `UndoableEditEvent` 携带 `getEdit()`（`UndoableEdit`）与 `getSource()`。`UndoableEdit` 提供 `undo/redo/canUndo/canRedo/die/addEdit/replaceEdit/isSignificant/getPresentationName` 等撤销/重做状态机方法。

## 2. Java 用户如何使用

构造：实现接口并注册到 `Document`（文本编辑产生可撤销编辑）：

```java
PlainDocument doc = new PlainDocument();
UndoableEditListener l = new UndoableEditListener() {
    public void undoableEditHappened(UndoableEditEvent e) {
        UndoableEdit edit = e.getEdit();   // 交给 UndoManager 管理
        undoManager.addEdit(edit);
    }
};
doc.addUndoableEditListener(l);
doc.insertString(0, "abc", null);   // 触发 undoableEditHappened
```

事件对象由 `AbstractDocument.fireUndoableEditUpdate` 构造 `new UndoableEditEvent(this, edit)`。

典型组合链路：`addUndoableEditListener → insertString → undoableEditHappened → e.getEdit() → edit.undo() 回退 → edit.redo() 重做`（`canUndo/canRedo` 随状态翻转）。

## 3. 开源代码实际用法调研

1. JDK `AbstractDocument.fireUndoableEditUpdate`（`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/AbstractDocument.java:306-310`）遍历监听器：
   ```java
   ((UndoableEditListener)listeners[i+1]).undoableEditHappened(e);
   ```
2. `AbstractDocument.addUndoableEditListener`（同文件 `:504`）注册监听器，`insertString/remove` 内部 `fireUndoableEditUpdate` 派发。
3. `AbstractUndoableEdit`（`src/java.desktop/share/classes/javax/swing/undo/AbstractUndoableEdit.java`）定义 `canUndo()==alive&&hasBeenDone`、`canRedo()==alive&&!hasBeenDone`，是本测试状态机断言依据。

## 4. 测试用例设计 + 自动化实现

黑盒设计（`UndoableEditListenerTest.testBlackBox()`）：
- 匿名实现记录事件。
- 构造 `UndoableEditEvent(source, new AbstractUndoableEdit())`，调用 `undoableEditHappened`，断言回调触发、`getSource()`/`getEdit()` round-trip、`isSignificant()` 默认 true。
- 对捕获的 `UndoableEdit` 走 undo/redo 状态机：初始 `canUndo()==true/canRedo()==false` → `undo()` 后翻转 → `redo()` 恢复。

白盒保留原有结构断言，新增 `undoableEditHappened(UndoableEditEvent)` 返回 void/抽象与 `declared method count == 1`。
