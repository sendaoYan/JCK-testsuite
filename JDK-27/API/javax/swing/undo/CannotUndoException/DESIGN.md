# javax.swing.undo.CannotUndoException 设计案

## 1. API 功能
`RuntimeException` 子类，当 `UndoableEdit` 被要求 `undo()` 而当前不可撤销时抛出，作为撤销框架的撤销失败信号。

- **构造器**：`CannotUndoException()`（无参，`message` 为 `null`）。
- 继承 `RuntimeException`/`Exception`/`Throwable` 的全部能力。

## 2. Java 用户如何使用
在实现或调用撤销框架时以 try/catch 捕获精确类型：

```java
try {
    edit.undo();
} catch (CannotUndoException e) {
    // 没有可撤销的编辑
}
```

典型组合链：`new UndoManager() → 空栈 undo → 捕获 CannotUndoException`。

## 3. 开源代码实际用法调研
- `src/java.desktop/share/classes/javax/swing/undo/AbstractUndoableEdit.java`：`undo()` 中 `if (!canUndo()) throw new CannotUndoException();`。
- `test/jdk/javax/swing/undo/UndoManager/bug4706533.java`：`manager.undo()` 后 `catch (CannotUndoException e) { // Expected }`，验证 `setLimit(0)` 后撤销不可用。

## 4. 测试用例设计 + 自动化实现
`CannotUndoExceptionTest.testBlackBox()` 覆盖：
- 无参构造、`getMessage()==null`、`instanceof RuntimeException`/`Exception`。
- throw/catch 精确类型捕获。
- `toString()` 以 `javax.swing.undo.CannotUndoException` 开头。
- 白盒保留结构断言（父类 `RuntimeException`、无接口）。
