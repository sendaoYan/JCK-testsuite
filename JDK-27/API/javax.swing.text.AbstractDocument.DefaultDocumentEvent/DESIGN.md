# javax.swing.text.AbstractDocument.DefaultDocumentEvent — DESIGN

## 1. API 功能
`CompoundEdit` 子类并实现 `DocumentEvent`，是文档插入/删除/属性变更时实际投递给监听器的编辑事件。方法分组：
- 事件元数据：`getType()`、`getOffset()`、`getLength()`、`getDocument()`。
- 元素变更：`getChange(Element)`。
- 编辑合并/描述：`addEdit(UndoableEdit)`、`isSignificant()`、`getPresentationName()`、`getUndoPresentationName()`、`getRedoPresentationName()`。
- 撤销/重做：`undo()`、`redo()`。
- 调试：`toString()`。

## 2. Java 用户如何使用
非 static 内部类，可直接 `doc.new DefaultDocumentEvent(offs, len, DocumentEvent.EventType.INSERT)` 构造以读取元数据；更常见的是通过 `DocumentListener.insertUpdate(DocumentEvent e)` 拿到真实事件并向下转型读取 `getType/getOffset/getLength/getDocument/getChange`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/swing/text/AbstractDocument.java:630`：`handleRemove` 里 `new DefaultDocumentEvent(offs, len, DocumentEvent.EventType.REMOVE)` 并 `chng.addEdit(...)` 后 `fireRemoveUpdate(chng)`。
- `src/java.desktop/share/classes/javax/swing/text/PlainDocument.java:176`：`insertUpdate(DefaultDocumentEvent chng, AttributeSet attr)` 按换行拆分行并 `chng.addEdit(ee)`。
- `test/jdk/javax/swing/text/AbstractDocument/8190763/TestCCEOnEditEvent.java`：验证编辑事件中元素变更的可观察行为。

## 4. 测试用例设计 + 自动化实现（`DefaultDocumentEventTest.testBlackBox`）
- 直接构造 `INSERT`/`REMOVE` 事件，验证 `getType/getOffset/getLength/getDocument/isSignificant` 及各 presentation name 非空、`toString` 非空。
- 通过 `DocumentListener` 捕获真实插入事件（含换行触发结构变更），验证 `getType/getOffset/getLength/getDocument` 与 `getChange(root)` 返回元素变更。
- 单行插入时 `getChange(root)` 为 null。
