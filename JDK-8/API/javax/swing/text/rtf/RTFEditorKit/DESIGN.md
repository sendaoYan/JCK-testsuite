# javax.swing.text.rtf.RTFEditorKit — DESIGN

## 1. API 功能
RTF 文本编辑器 Kit（`extends StyledEditorKit`）。支持把 RTF 流读入 `Document` 与把 `Document` 写回 RTF。
- 构造器：`RTFEditorKit()`。
- 查询/文档：`getContentType()`（"text/rtf"）、`createDefaultDocument()`（StyledDocument）。
- 读写：`read(InputStream, Document, int)`、`read(Reader, Document, int)`、`write(OutputStream, Document, int, int)`、`write(Writer, Document, int, int)`。

## 2. Java 用户如何使用
`RTFEditorKit k = new RTFEditorKit(); Document d = k.createDefaultDocument(); k.read(new StringReader("{\\rtf1\\ansi Hello\\par}"), d, 0);` 后 `d.getText(...)`；或 `k.write(outStream, d, 0, len)` 生成 RTF。`write(Writer,...)` 因 RTF 为 8-bit 格式抛 IOException("RTF is an 8-bit format")。

## 3. 开源代码实际用法调研
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/JEditorPane.java`：defaultEditorKitMap 注册 "text/rtf"/"application/rtf" → RTFEditorKit。
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/rtf/RTFEditorKit.java`：`write(Writer,...)` 直接 `throw new IOException("RTF is an 8-bit format");`。
- `~/git/jdk8u/jdk/test/javax/swing/text/rtf/`：RTF 读写回归系列。

## 4. 测试用例设计（黑盒，`RTFEditorKitTest`：testShape/testBasics/testRoundTrip/testNegatives，JDK-8 33 断言）
1. testBasics：getContentType=="text/rtf"、createDefaultDocument 为 StyledDocument、clone 独立、`getActions()` 数量版本分支（JDK-8 73 / JDK-27 75）。
2. testRoundTrip：write(OutputStream) 输出含 "{\\rtf1" 头与 \\par；"Line1\\nLine2" roundtrip 得 "Line1\\nLine2\\n"（长度 12，尾换行契约）；`\\'e9` 8-bit 转义与 \\uN unicode 转义读入 "escé A\\n"；非 RTF 输入 → 空文档；非法 pos 写 tolerated。
3. testNegatives（精确异常）：`write(Writer,...)` → IOException "RTF is an 8-bit format"；null Document/null 流等 4 个 NPE。
