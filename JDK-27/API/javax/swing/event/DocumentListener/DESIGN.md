# javax.swing.event.DocumentListener 测试设计

## 1. API 功能

`DocumentListener` 监听 `javax.swing.text.Document` 内容变化，继承 `java.util.EventListener`，三个回调：

- `void insertUpdate(DocumentEvent e)`：插入文本后触发。
- `void removeUpdate(DocumentEvent e)`：删除文本后触发。
- `void changedUpdate(DocumentEvent e)`：属性/样式变化（非纯插入删除）后触发。

配套事件 `DocumentEvent`（接口）暴露 `getOffset()/getLength()/getDocument()/getType()/getChange(Element)`；`DocumentEvent.EventType` 提供常量 `INSERT/REMOVE/CHANGE`。

## 2. Java 用户如何使用

构造：实现接口并注册到文档（常用 `PlainDocument`/`DefaultStyledDocument`）：

```java
PlainDocument doc = new PlainDocument();
DocumentListener l = new DocumentListener() {
    public void insertUpdate(DocumentEvent e)  { /* 插入 */ }
    public void removeUpdate(DocumentEvent e)  { /* 删除 */ }
    public void changedUpdate(DocumentEvent e) { /* 样式 */ }
};
doc.addDocumentListener(l);
doc.insertString(0, "hello", null);   // 触发 insertUpdate，offset=0 length=5 type=INSERT
doc.remove(1, 3);                     // 触发 removeUpdate，offset=1 length=3 type=REMOVE
```

样式变更（`DefaultStyledDocument.setCharacterAttributes`）触发 `changedUpdate`（type=CHANGE）。

典型组合链路：`insertString → insertUpdate → getType()==INSERT → getOffset()/getLength() → remove → removeUpdate`，用户据此增量刷新视图。

## 3. 开源代码实际用法调研

1. JDK 实现 `JTextComponent.AccessibleJTextComponent` 实现 `DocumentListener`（`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/JTextComponent.java:2549`），文档变化时刷新无障碍信息。
2. `AbstractDocument` 注册/分发 `UndoableEditListener` 同文件也负责 `DocumentListener` 派发（`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/AbstractDocument.java`），`fireInsertUpdate/fireRemoveUpdate/fireChangedUpdate` 是三个回调的触发源。
3. `JTextComponent` 的 `DocumentListener` 相关注释（`:232,:289`）与 `addDocumentListener` 派发说明了文档监听在编辑器视图刷新中的核心地位。

## 4. 测试用例设计 + 自动化实现

黑盒设计（`DocumentListenerTest.testBlackBox()`）：
- 匿名实现记录最近回调名与事件对象。
- 用 `PlainDocument.insertString`/`remove` 触发**真实** `insertUpdate`/`removeUpdate`，断言 `getType()==INSERT/REMOVE`、`getOffset()/getLength()`、`getDocument()` 与触发文档一致。
- 用 `DefaultStyledDocument.setCharacterAttributes` 触发**真实** `changedUpdate`，断言 `getType()==CHANGE` 与 offset/length/document。
- 断言三个 `EventType` 常量互异且 `toString()` 为 `INSERT/REMOVE/CHANGE`。

白盒保留原有结构断言，新增三方法签名/抽象性与 `declared method count == 3`。全程 headless（纯文本模型）。
