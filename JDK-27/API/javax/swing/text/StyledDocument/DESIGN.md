# javax.swing.text.StyledDocument — DESIGN

## 1. API 功能
扩展 `Document` 的样式文本接口。方法分组：
- 样式：`addStyle(String,Style)`、`removeStyle(String)`、`getStyle(String)`。
- 属性：`setCharacterAttributes(int,int,AttributeSet,boolean)`、`setParagraphAttributes(int,int,AttributeSet,boolean)`。
- 逻辑样式：`setLogicalStyle(int,Style)`、`getLogicalStyle(int)`。
- 元素：`getParagraphElement(int)`、`getCharacterElement(int)`。
- 颜色/字体：`getForeground(AttributeSet)`、`getBackground(AttributeSet)`、`getFont(AttributeSet)`。

## 2. Java 用户如何使用
接口契约经 `DefaultStyledDocument` 使用：`StyledDocument doc = new DefaultStyledDocument(); doc.insertString(0,"styled text",null); Style s = doc.addStyle("h",null); doc.setCharacterAttributes(0,6,attr,false); doc.setLogicalStyle(0,s); Color c = doc.getForeground(s);`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/swing/text/DefaultStyledDocument.java:53`：`public class DefaultStyledDocument extends AbstractDocument implements StyledDocument`。
- `test/jdk/javax/swing/text/DefaultStyledDocument/bug4472852.java`：`StyledDocument doc = new DefaultStyledDocument();` 并调用 `getDefaultRootElement`、`setCharacterAttributes`。
- `src/java.desktop/share/classes/javax/swing/JTextPane.java`：以 `StyledDocument` 作为其文档模型类型。

## 4. 测试用例设计 + 自动化实现（`StyledDocumentTest.testBlackBox`）
- 以 `DefaultStyledDocument` 实现走接口：insert/getText/getLength（继承 Document）。
- `addStyle/getStyle/removeStyle`、`setLogicalStyle/getLogicalStyle`。
- `setCharacterAttributes` 反映到 `getCharacterElement`，`setParagraphAttributes` 反映到 `getParagraphElement`。
- `getForeground/getBackground/getFont` 经 `StyleConstants` 解析固定颜色/字体族。
