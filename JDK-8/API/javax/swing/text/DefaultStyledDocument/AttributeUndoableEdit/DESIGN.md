# javax.swing.text.DefaultStyledDocument.AttributeUndoableEdit — DESIGN

## 1. API 功能
`AbstractUndoableEdit` 子类，记录对某元素属性集的变更。方法分组：
- 构造：`AttributeUndoableEdit(Element, AttributeSet, boolean)`（boolean 表示是否先清空原有属性再添加）。
- 撤销/重做：`redo()`、`undo()`。
- 受保护字段：`newAttributes`、`copy`、`isReplacing`、`element`。

## 2. Java 用户如何使用
static 嵌套类，可 `new DefaultStyledDocument.AttributeUndoableEdit(element, newAttrs, isReplacing)` 构造。编辑已假定生效（`hasBeenDone=true`），因此先 `undo()` 恢复构造时拷贝的 `copy`，再 `redo()` 重新应用 `newAttributes`；`isReplacing=true` 时 redo 会先清空旧属性。

## 3. 开源代码实际用法调研（来源：jdk8u 源码 + jdk/test）
- `jdk/src/share/classes/javax/swing/text/DefaultStyledDocument.java:535`：`changes.addEdit(new AttributeUndoableEdit(run, sCopy, replace));` 在 `setCharacterAttributes` 里按 run 逐个创建。
- `jdk/src/share/classes/javax/swing/text/DefaultStyledDocument.java:581`：`setParagraphAttributes` 里 `new AttributeUndoableEdit(paragraph, sCopy, replace)`。
- `jdk/src/share/classes/javax/swing/text/DefaultStyledDocument.java:2541`：类定义与 `undo/redo` 的 `removeAttributes(as)+addAttributes(...)` 逻辑。

## 4. 测试用例设计 + 自动化实现
`testUndoRedo(replacing=false/true)`（32 断言）：
- 类型面：public 类、父类 AbstractUndoableEdit、方法 undo/redo/isSignificant。
- 状态机：新编辑处于 done 态（canUndo、直接 redo 抛 CannotRedoException）；undo 先恢复快照、redo 应用记录的新属性；replacing=true 时 redo 清除旧值、false 保留；undo→redo→undo 循环重复可用。
- 全程在暴露 writeLock 的文档子类锁内执行。
