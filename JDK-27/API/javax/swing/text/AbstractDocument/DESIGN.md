# javax.swing.text.AbstractDocument — DESIGN

## 1. API 功能
`AbstractDocument` 是 `Document` 接口的抽象基类，实现了大部分公共契约，只把 `getDefaultRootElement()` 和 `getParagraphElement(int)` 留给子类。方法分组：
- 数据操作：`getLength()`、`getText(int,int)`、`getText(int,int,Segment)`、`insertString(int,String,AttributeSet)`、`remove(int,int)`、`replace(int,int,String,AttributeSet)`。
- 位置/结构：`createPosition(int)`、`getStartPosition()`、`getEndPosition()`、`getRootElements()`、`getDefaultRootElement()`、`getBidiRootElement()`、`getParagraphElement(int)`。
- 属性：`getDocumentProperties()`、`setDocumentProperties(Dictionary)`、`getProperty(Object)`、`putProperty(Object,Object)`。
- 监听器：`addDocumentListener`、`removeDocumentListener`、`getDocumentListeners`、`addUndoableEditListener`、`removeUndoableEditListener`、`getUndoableEditListeners`。
- 杂项：`render(Runnable)`、`setDocumentFilter`、`getDocumentFilter`、`getAsynchronousLoadPriority`、`setAsynchronousLoadPriority`、`dump(PrintStream)`。

## 2. Java 用户如何使用
用户不能直接 `new AbstractDocument`，而是 `new PlainDocument()`（或 `new DefaultStyledDocument()`）后调用继承的方法。典型组合链：
`doc.insertString(0, "Hello", null)` → `doc.getLength()` → `doc.getText(0, doc.getLength())` → `doc.remove(0, 5)` → `doc.createPosition(0)` 观察偏移跟踪 → `doc.putProperty("k","v")` → `doc.getProperty("k")` → `doc.addUndoableEditListener(...)` 捕获编辑并 `undo()/redo()`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/swing/text/PlainDocument.java:114`：`PlainDocument` 覆盖 `insertString`，先按 `filterNewlines` 属性过滤换行再 `super.insertString(...)`。
- `src/java.desktop/share/classes/javax/swing/text/DefaultStyledDocument.java:96`：`DefaultStyledDocument` 通过 `new GapContent(BUFFER_SIZE_DEFAULT)` 与 `StyleContext` 组合出内容存储。
- `test/jdk/javax/swing/text/DefaultStyledDocument/bug4472852.java`：`StyledDocument doc = new DefaultStyledDocument(); doc.insertString(0, "this", null); Element root = doc.getDefaultRootElement(); doc.setCharacterAttributes(...)`。
- `test/jdk/javax/swing/text/AbstractDocument/6968363/Test6968363.java`：依赖 `PlainDocument`/`Position`/`Segment` 的偏移与位置跟踪。

## 4. 测试用例设计 + 自动化实现（`AbstractDocumentTest.testBlackBox`）
- 用 `PlainDocument` 实例走主链路：insertString/getLength/getText/remove/replace 往返。
- 元素树遍历：`getDefaultRootElement()`、`getParagraphElement(0)`、子元素区间。
- `createPosition` 偏移跟踪（在位置前插入则右移）、`getStartPosition/getEndPosition`。
- `getRootElements()` 返回 2 元素（默认根 + bidi 根）。
- 属性 `putProperty/getProperty`（含 null 删除）、`setDocumentProperties`。
- `getAsynchronousLoadPriority` 默认 -1 与往返、`setDocumentFilter/getDocumentFilter`。
- `render(Runnable)` 执行回调；`addDocumentListener/removeDocumentListener` 通知计数。
- `addUndoableEditListener` 捕获编辑后 `undo()/redo()` 反转/重放插入。
- 边界：`getText(0,-1)`、越界 `insertString/remove` 均抛 `BadLocationException`。
