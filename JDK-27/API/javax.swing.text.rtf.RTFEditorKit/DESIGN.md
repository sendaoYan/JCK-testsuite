# javax.swing.text.rtf.RTFEditorKit — DESIGN

## 1. API 功能
RTF 文本编辑器 Kit（`extends StyledEditorKit`）。支持把 RTF 流读入 `Document` 与把 `Document` 写回 RTF。
- 构造器：`RTFEditorKit()`。
- 查询/文档：`getContentType()`（"text/rtf"）、继承 `createDefaultDocument()`（StyledDocument）。
- 读写：`read(InputStream, Document, int)`、`read(Reader, Document, int)`、`write(OutputStream, Document, int, int)`、`write(Writer, Document, int, int)`。

## 2. Java 用户如何使用
`RTFEditorKit k = new RTFEditorKit();` → `Document d = k.createDefaultDocument();` → `k.read(new StringReader("{\\rtf1\\ansi Hello\\par}"), d, 0);` → `d.getText(...)`；或 `k.write(outStream, d, 0, d.getLength())` 生成 RTF。`write(Writer,...)` 因 RTF 为 8-bit 格式抛 `IOException`。

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JEditorPane.java`：
  ```java
  defaultEditorKitMap.put("text/rtf", "javax.swing.text.rtf.RTFEditorKit");
  defaultEditorKitMap.put("application/rtf", "javax.swing.text.rtf.RTFEditorKit");
  ```
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/rtf/RTFReadBGColorTest.java`：`text.setEditorKit(new RTFEditorKit());` 后用 `StyledDocument` 读样式属性（headful）。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/rtf/RTFEditorKit.java`（自用）：`write(Writer,...)` 直接 `throw new IOException("RTF is an 8-bit format");`。

## 4. 测试用例设计（黑盒部分，`RTFEditorKitTest.testBlackBox`）
1. `getContentType()=="text/rtf"`；`createDefaultDocument()` 为 StyledDocument。
2. `read(Reader)` 最小 RTF：文档含 "Hello World"、长度 >0。
3. `read(InputStream)` 捕获 "Stream"。
4. 在非零 offset 读入：保留前缀并追加内容。
5. `write(OutputStream)` 回环含 "Hello World"。
6. `write(Writer)` 抛 `IOException`（精确类型）。
7. 非 RTF 文本读入 → 空文档（长度 0）。
