# javax.swing.undo.CannotRedoException 设计案

## 1. API 功能
`RuntimeException` 子类，当 `UndoableEdit` 被要求 `redo()` 而当前不可重做时抛出，作为撤销框架的重做失败信号。

- **构造器**：`CannotRedoException()`（无参，`message` 为 `null`）。
- 继承 `RuntimeException`/`Exception`/`Throwable` 的全部能力（`getMessage`、`toString`、可捕获）。

## 2. Java 用户如何使用
在实现或调用撤销框架时以 try/catch 捕获精确类型：

```java
try {
    edit.redo();
} catch (CannotRedoException e) {
    // 没有可重做的编辑
}
```

典型组合链：`new UndoManager() → addEdit → undo → 顶部 redo → 捕获 CannotRedoException`。

## 3. 开源代码实际用法调研
- `src/java.desktop/share/classes/javax/swing/undo/AbstractUndoableEdit.java`：`redo()` 中 `if (!canRedo()) throw new CannotRedoException();`，是抛出的权威来源。
- `src/java.desktop/share/classes/javax/swing/text/StringContent.java`：`RemoveUndo.redo()` 在 `BadLocationException` 时 `throw new CannotRedoException();`。

## 4. 测试用例设计 + 自动化实现
`CannotRedoExceptionTest.testBlackBox()` 覆盖：
- 无参构造、`getMessage()==null`、`instanceof RuntimeException`/`Exception`。
- throw/catch 精确类型捕获。
- `toString()` 以 `javax.swing.undo.CannotRedoException` 开头。
- 白盒保留结构断言（父类 `RuntimeException`、无接口）。
