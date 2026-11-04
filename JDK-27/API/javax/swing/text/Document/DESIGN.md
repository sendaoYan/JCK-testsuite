# javax.swing.text.Document — DESIGN

## 1. API 功能
文本模型的顶层接口。方法分组：
- 常量：`StreamDescriptionProperty`（"stream"）、`TitleProperty`（"title"）。
- 数据：`getLength()`、`getText(int,int)`、`getText(int,int,Segment)`、`insertString(int,String,AttributeSet)`、`remove(int,int)`。
- 位置/结构：`getStartPosition()`、`getEndPosition()`、`createPosition(int)`、`getRootElements()`、`getDefaultRootElement()`。
- 监听器：`addDocumentListener`、`removeDocumentListener`、`addUndoableEditListener`、`removeUndoableEditListener`。
- 属性：`getProperty(Object)`、`putProperty(Object,Object)`；渲染：`render(Runnable)`。

## 2. Java 用户如何使用
接口契约经 `PlainDocument`/`DefaultStyledDocument` 使用：`Document doc = new PlainDocument(); doc.insertString(0,"text",null); String s = doc.getText(0,doc.getLength()); doc.createPosition(0); doc.putProperty("title","T"); doc.render(() -> {...});` 并注册 `DocumentListener` 观察插入/删除。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/swing/text/JTextComponent.java`：内部以 `Document` 引用持有模型，调用 `getText`、`insertString`、`remove`。
- `test/jdk/javax/swing/text/AbstractDocument/6968363/Test6968363.java`：`import javax.swing.text.Document;` 并组合 `PlainDocument`/`Position`/`Segment`。
- `src/java.desktop/share/classes/javax/swing/text/DefaultEditorKit.java:133`：`createDefaultDocument()` 返回 `new PlainDocument()`（Document 实现）。

## 4. 测试用例设计 + 自动化实现（`DocumentTest.testBlackBox`）
- 校验两个属性常量值。
- 以 `Document doc = new PlainDocument()` 走 insertString/getLength/getText/remove 往返。
- `getStartPosition/getEndPosition/createPosition` 偏移跟踪。
- `getRootElements/getDefaultRootElement`、`putProperty/getProperty`（含 null 删除）、`render` 执行回调。
- 监听器：插入+删除各通知一次，移除后不再通知。
- 边界：越界 `insertString`、`getText` 负长度抛 `BadLocationException`。
