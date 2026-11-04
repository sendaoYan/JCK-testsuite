# javax.swing.text.DefaultEditorKit — DESIGN

## 1. API 功能
纯文本编辑器策略实现（`extends EditorKit`）。核心方法分组：
- 构造器：`DefaultEditorKit()`。
- 内容类型/视图：`getContentType()`（"text/plain"）、`getViewFactory()`（默认 null）、`createCaret()`（默认 null）。
- 动作集：`getActions()` 返回 60 个内置 Action（InsertContentAction、InsertBreakAction、InsertTabAction、DefaultKeyTypedAction 等），名称由公开常量定义（如 `insertContentAction="insert-content"`）。
- 文档：`createDefaultDocument()` 返回 PlainDocument。
- 读写：`read(Reader/InputStream, Document, int)` 与 `write(Writer/OutputStream, Document, int, int)`，处理 `\n`/`\r`/`\r\n` 归一化，用 `EndOfLineStringProperty` 记录文档换行约定。

## 2. Java 用户如何使用
`new DefaultEditorKit()` → `getContentType()`/`getActions()`/`createDefaultDocument()`；`read(new StringReader("a\nb"), doc, 0)` 把文本载入文档并设置 EOL 属性；`write(new StringWriter(), doc, 0, doc.getLength())` 输出文本并把 `\n` 按 EOL 属性翻译。

## 3. 开源代码实际用法调研
- `src/java.desktop/share/classes/javax/swing/JEditorPane.java`（`kit.createDefaultDocument()`）：JEditorPane 用 EditorKit 作为文档/视图工厂，`setPage`/初始化都调 `getEditorKit().createDefaultDocument()`。
- `src/java.desktop/share/classes/javax/swing/plaf/basic/BasicTextUI.java`：`if (getEditorKit(editor) instanceof DefaultEditorKit)`，并查 `map.get(DefaultEditorKit.insertBreakAction)` 判断是否为 `DefaultEditorKit.InsertBreakAction`；`editor.setDocument(getEditorKit(editor).createDefaultDocument())`。
- `src/java.desktop/share/classes/javax/swing/text/JTextComponent.java`：`getKeymapTable()` 初始化默认 keymap 时 `binding.setDefaultAction(new DefaultEditorKit.DefaultKeyTypedAction())`。

## 4. 测试用例设计（黑盒部分，`DefaultEditorKitTest.testBlackBox`）
1. 内容类型/视图/插入符号/文档类型断言（text/plain、null、null、PlainDocument）。
2. getActions 非空 + 关键动作名存在 + 两次调用实例稳定。
3. read 归一化：`\n`、`\r\n`、`\r` 三种输入各自映射为 `\n` 并设置对应 EOL 属性；InputStream 读 ASCII。
4. write：按显式 EOL 属性翻译 `\n`→`\r\n`；写子串；OutputStream 写回 ASCII。
5. 非法 pos/len 抛精确 `BadLocationException`。
