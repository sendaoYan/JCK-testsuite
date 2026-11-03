# javax.swing.JTextPane 设计案

## 1. API 功能
`JTextPane` 是样式化文本组件（继承 `JEditorPane`）。方法分组：
- 文档：`getStyledDocument/setStyledDocument/setDocument`。
- 样式：`addStyle/removeStyle/getStyle/setLogicalStyle/getLogicalStyle`。
- 属性：`getCharacterAttributes/getParagraphAttributes/getInputAttributes/setCharacterAttributes/setParagraphAttributes`。

## 2. Java 用户如何使用
用户编辑带样式的文本：
```java
JTextPane pane = new JTextPane();
Style style = pane.addStyle("bold", null);
pane.setLogicalStyle(style);
```
典型链路：`new JTextPane` → `addStyle/getStyle` → `setCharacterAttributes`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/JTextPane.java` 是 JDK 自身实现（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JTextPane.java`）。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `JEditorPane`。
- 行为（黑盒）：`testBlackBox()` 覆盖 `StyledDocument`、`setText/getText`、样式 add/get/remove/logical、字符/段落/输入属性与 set，以及 `DefaultStyledDocument` 构造。
