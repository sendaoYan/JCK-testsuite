# javax.swing.text.EditorKit — DESIGN

## 1. API 功能
抽象基类（`implements Cloneable, Serializable`），定义某类文本内容的编辑器策略工厂。抽象方法：`getContentType()`、`getViewFactory()`、`getActions()`、`createCaret()`、`createDefaultDocument()`、`read`/`write`（InputStream/Reader、OutputStream/Writer 两个重载）。具体方法：`clone()`（默认 Object.clone，失败返回 null）、`install(JEditorPane)`/`deinstall(JEditorPane)`（默认空实现）。

## 2. Java 用户如何使用
通过具体子类（DefaultEditorKit、StyledEditorKit、HTMLEditorKit）使用：`new DefaultEditorKit()` → `getContentType()`/`createDefaultDocument()`/`read`/`write`；JEditorPane 内部用 `getEditorKit().createDefaultDocument()` 建文档。

## 3. 开源代码实际用法调研
- `src/java.desktop/share/classes/javax/swing/JEditorPane.java`：`Document doc = kit.createDefaultDocument();`，`public EditorKit getEditorKit()` 与 `createDefaultEditorKit()` 工厂。
- `src/java.desktop/share/classes/javax/swing/plaf/basic/BasicTextUI.java`：`getEditorKit(editor) instanceof DefaultEditorKit` 分支处理动作；`editor.setDocument(getEditorKit(editor).createDefaultDocument())`。
- `src/java.desktop/share/classes/javax/swing/text/html/HTMLEditorKit.java`：`TextAction.augmentList(super.getActions(), defaultActions)` 扩展动作集（EditorKit.getActions 契约）。
- `src/java.desktop/share/classes/javax/swing/text/StyledEditorKit.java`：`public Object clone()` 覆盖基类 clone。

## 4. 测试用例设计（黑盒部分，`EditorKitTest.testBlackBox`）
1. DefaultEditorKit 满足 EditorKit 契约：content type、default document 是 Document、actions 非空。
2. clone 返回同类型不同实例、content type 一致。
3. install/deinstall 对 DefaultEditorKit 为 no-op。
4. 经 EditorKit 引用做 read/write 往返（"hi"）。
5. 第二个具体实现 StyledEditorKit 也是 EditorKit，content type/视图工厂契约成立。
