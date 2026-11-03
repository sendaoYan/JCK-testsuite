# javax.swing.text.PlainDocument — DESIGN

## 1. API 功能
`AbstractDocument` 的纯文本实现，默认用 `GapContent` 存储，元素结构为按行映射。方法分组：
- 常量：`tabSizeAttribute`（"tabSize"）、`lineLimitAttribute`（"lineLimit"）。
- 构造：`PlainDocument()`、`PlainDocument(Content)`。
- 数据：`insertString(int,String,AttributeSet)`（支持 `filterNewlines` 属性过滤换行）。
- 结构：`getDefaultRootElement()`、`getParagraphElement(int)`。
- 保护钩子：`createDefaultRoot()`、`insertUpdate`、`removeUpdate`。

## 2. Java 用户如何使用
`new PlainDocument()` → `insertString(0,"one\ntwo",null)` → `getText(0,getLength())` → `getDefaultRootElement().getElementCount()` 得行数 → `getParagraphElement(pos)` 定位行；`putProperty("filterNewlines", Boolean.TRUE)` 后插入的 `\n` 被替换为空格。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/swing/JTextField.java:379`：`createDefaultModel()` 返回 `new PlainDocument()`。
- `src/java.desktop/share/classes/javax/swing/JTextArea.java:260`：同样返回 `new PlainDocument()`。
- `src/java.desktop/share/classes/javax/swing/text/DefaultEditorKit.java:133`：`return new PlainDocument();`。
- `test/jdk/javax/swing/text/AbstractDocument/6968363/Test6968363.java`：使用 `PlainDocument` 做偏移/位置跟踪。

## 4. 测试用例设计 + 自动化实现（`PlainDocumentTest.testBlackBox`）
- 校验常量与默认 tab 大小 8；insert/getText/remove 往返。
- 行结构：多行插入后 `getDefaultRootElement` 行数、`getParagraphElement` 映射与各行区间。
- `filterNewlines` 属性开启后 `\n` 变空格；`PlainDocument(Content)` 构造可用；null/空插入为 no-op。
- 边界：越界 `insertString/remove` 抛 `BadLocationException`。
