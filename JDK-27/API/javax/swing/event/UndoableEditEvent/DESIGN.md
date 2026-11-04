# UndoableEditEvent 行为测试设计

## 1. API 功能
`javax.swing.event.UndoableEditEvent` 是可撤销编辑事件，继承 `java.util.EventObject`。

- **构造器**：`UndoableEditEvent(Object source, UndoableEdit edit)`。
- **查询**：`getEdit()`（携带的 UndoableEdit）、继承的 `getSource()`。

## 2. Java 用户如何使用
`Document` 实现（如 `AbstractDocument`）派发，用户实现 `UndoableEditListener`：

```java
doc.addUndoableEditListener(e -> {
    UndoableEdit edit = e.getEdit();
    undoManager.addEdit(edit);               // 入撤销栈
});
```

每个方法典型调用：`new UndoableEditEvent(doc, edit)` 构造 → `getEdit()` 取得编辑 → 调用编辑的 `undo()/redo()/getPresentationName()`。

## 3. 开源代码实际用法调研
1. `jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/AbstractDocument.java:649/778`：
   ```java
   fireUndoableEditUpdate(new UndoableEditEvent(this, chng));
   fireUndoableEditUpdate(new UndoableEditEvent(this, e));
   ```
   内部在文档变更后构造 `UndoableEditEvent` 通知撤销栈。
2. `test/jdk/javax/swing/text/AbstractDocument/8190763/TestCCEOnEditEvent.java` 直接消费 `UndoableEditEvent` 做回归验证。

## 4. 测试用例设计 + 自动化实现
- 白盒：保留原结构断言（继承 EventObject、public、非 abstract、无接口）。
- 黑盒：用 `AbstractUndoableEdit` 与自定义 edit 构造事件，验证 `getSource/getEdit` 身份 round-trip；自定义 edit 的 `getPresentationName()` 经 `getEdit()` 正确透传，形成「构造 → 取编辑 → 调用编辑方法」组合链。
- 自动化：`testWhiteBox()` + `testBlackBox()`，headless-safe。
