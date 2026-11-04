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

## 3. 开源代码实际用法调研（来源：jdk8u 源码 + jdk/test）
- `jdk/src/share/classes/javax/swing/text/DefaultStyledDocument.java:53`：`public class DefaultStyledDocument extends AbstractDocument implements StyledDocument`。
- `jdk/test/javax/swing/text/DefaultStyledDocument/bug4472852.java`：`StyledDocument doc = new DefaultStyledDocument();` 并调用 `getDefaultRootElement`、`setCharacterAttributes`。
- `jdk/src/share/classes/javax/swing/JTextPane.java`：以 `StyledDocument` 作为其文档模型类型。

## 4. 测试用例设计 + 自动化实现
`testUmbrella`（31 断言）：
- 接口形状：12 个方法（无 getStyleNames——那是 DefaultStyledDocument 的具体方法，反射锁定反例）。
- DefaultStyledDocument 实现 StyledDocument：段/run 结构、逻辑 style 存在且可换、段落属性接受、style 注册表与上下文共享、getForeground/getBackground/getFont 委托派生。
