# javax.swing.text.DefaultStyledDocument — DESIGN

## 1. API 功能
`StyledDocument` 的标准实现（继承 `AbstractDocument`）。方法分组：
- 构造：`DefaultStyledDocument()`、`DefaultStyledDocument(StyleContext)`、`DefaultStyledDocument(Content, StyleContext)`。
- 结构：`getDefaultRootElement()`、`getParagraphElement(int)`、`getCharacterElement(int)`、`removeElement(Element)`。
- 样式：`addStyle/getStyle/removeStyle/getStyleNames`、`setLogicalStyle/getLogicalStyle`。
- 属性：`setCharacterAttributes`、`setParagraphAttributes`。
- 颜色/字体解析：`getForeground/getBackground/getFont`。
- 缓冲构建（protected）：`create(ElementSpec[])`、`insert(int,ElementSpec[])`。

## 2. Java 用户如何使用
`new DefaultStyledDocument()` → `insertString(0,"abc\ndef",null)` → `getParagraphElement(0)`/`getCharacterElement(0)` 定位 → `addStyle("s",null)` 创建样式 → `setCharacterAttributes(0,3,attr,false)` 上色 → `setLogicalStyle(0,style)` 指定段落逻辑样式 → `getForeground/style` 解析颜色。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/swing/text/StyledEditorKit.java:104`：`createDefaultDocument()` 返回 `new DefaultStyledDocument()`。
- `test/jdk/javax/swing/text/DefaultStyledDocument/bug4472852.java`：`StyledDocument doc = new DefaultStyledDocument(); doc.insertString(0,"this",null); doc.setCharacterAttributes(...)`。
- `test/jdk/javax/swing/text/DefaultStyledDocument/DocNegLenCharAttrTest.java`：`doc = new DefaultStyledDocument(); text.setDocument(doc); doc.setCharacterAttributes(6,-5,attr,true)`（验证负长度不抛异常）。

## 4. 测试用例设计 + 自动化实现（`DefaultStyledDocumentTest.testBlackBox`）
- 根为 section、`getParagraphElement/getCharacterElement` 结构随换行更新。
- 文本 insert/getText/remove 往返。
- 样式 `addStyle/getStyle/removeStyle/getStyleNames`（含 default）。
- `setLogicalStyle/getLogicalStyle` 往返。
- `setCharacterAttributes/setParagraphAttributes` 反映到元素属性。
- `getForeground/getBackground/getFont` 经 `StyleConstants` 解析固定颜色/字体族。
- `removeElement` 删除段落文本与结构；`DefaultStyledDocument(StyleContext)` 构造可用。
