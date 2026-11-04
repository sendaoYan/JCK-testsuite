# javax.swing.text.JTextComponent — DESIGN

## 1. API 功能
Swing 文本组件基类（`public abstract class JTextComponent extends JComponent implements Scrollable, Accessible`）。核心方法分组：
- 文本：`setText(String)/getText()/getText(int,int)/read(Reader,Object)/write(Writer)`。
- 光标/选区：`setCaretPosition/getCaretPosition/moveCaretPosition/select/selectAll/setSelectionStart/End/getSelectionStart/End/getSelectedText/replaceSelection`。
- 文档模型：`setDocument/getDocument/setCaret/getCaret/setHighlighter/getHighlighter`。
- 剪贴板：`copy()/cut()/paste()`。
- 可编辑：`setEditable/isEditable`。
- 键盘/动作：`setKeymap/getKeymap/addKeymap/removeKeymap/getKeymap/loadKeymap/getActions`。
- 拖放：`setDragEnabled/getDragEnabled/setDropMode/getDropMode/getDropLocation`。
- 其他：`setFocusAccelerator/getFocusAccelerator/setCaretColor/getCaretColor/...`。

## 2. Java 用户如何使用
`new JTextArea()/JTextField()`（子类）→ `setText(...)` → `setCaretPosition(...)`/`select(...)` → `copy()/cut()/paste()` → `setDocument(new PlainDocument())` → `setEditable(false)` 等；`getText(offset,len)` 越界抛 `BadLocationException`，光标/选区越界抛 `IllegalArgumentException`。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/JTextComponent.java`（`setText`）：`AbstractDocument` 上 `replace(0, doc.getLength(), t, null)`；`(text==null||text.length()==0)` 时仅删除。
- 同文件 `setCaretPosition`/`moveCaretPosition`：`pos > doc.getLength() || pos < 0` 抛 `IllegalArgumentException`。
- 同文件 `DefaultTransferHandler`：`exportToClipboard` 读选区 → `StringSelection` → `clipboard.setContents`；`importData` → `replaceSelection`。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/JTextComponent/8156217/TextSelectionTest.java`：`SwingUtilities.invokeAndWait` + `JFrame` + `JTextArea` 做文本选择交互。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/DefaultEditorKit/bug4265242.java`：`getActions()` 遍历 + `getValue(Action.NAME)`。

## 4. 测试用例设计（黑盒部分，`JTextComponentTest.testBlackBox`）
1. setText/getText 往返；setText(null) 清空。
2. getText(offset,len) 子串；-1/负长/超长 → BadLocationException。
3. setCaretPosition/moveCaretPosition 往返；越界 → IllegalArgumentException。
4. select/setSelectionStart/End/getSelectedText/selectAll 组合。
5. setEditable/isEditable 往返。
6. setDocument(new PlainDocument())/getDocument 往返；caret/highlighter/keymap/actions 非空。
7. setFocusAccelerator/getFocusAccelerator 往返（默认 '\0'）。
8. setDragEnabled/getDragEnabled 往返。
9. copy→paste 与 cut→paste 剪贴板往返（源文本/选区语义 + 目标插入）。
