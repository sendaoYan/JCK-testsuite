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

## 3. 开源代码实际用法调研（来源：jdk8u 源码 + jdk/test）
- `jdk/src/share/classes/javax/swing/text/AbstractDocument.java:630`：`handleRemove` 里 `new DefaultDocumentEvent(offs, len, DocumentEvent.EventType.REMOVE)` 并 `chng.addEdit(...)` 后 `fireRemoveUpdate(chng)`。
- `jdk/src/share/classes/javax/swing/text/PlainDocument.java:176`：`insertUpdate(DefaultDocumentEvent chng, AttributeSet attr)` 按换行拆分行并 `chng.addEdit(ee)`。
- `jdk/test/javax/swing/text/AbstractDocument/8190763/TestCCEOnEditEvent.java`：验证编辑事件中元素变更的可观察行为。

## 4. 测试用例设计 + 自动化实现
`testInsertEvent/testRemoveEvent/testUndoThroughEvent/testElementChange`（48 断言）：
- 事件载荷：getOffset/getLength/getType/getDocument 精确（INSERT/REMOVE 各走查）；单行纯文本插入无结构变化。
- EventType 反例：是 final 类而非枚举（无 values()），INSERT/REMOVE/CHANGE toString 精确且三者互异。
- 事件本身是 UndoableEdit：isSignificant true、新事件只能 undo；presentationName "addition"/undo 名 "deletion" 精确；经事件 undo() 回滚插入。
- ElementChange：DefaultStyledDocument 段落级插入产生 ElementEdit——section 的 index=1/added=1/removed=0；段内合并 index=0/added=1/removed=1。
