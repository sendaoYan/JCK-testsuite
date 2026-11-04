# javax.swing.JEditorPane 设计案

## 1. API 功能
`JEditorPane` 是可编辑文本组件（继承 `JTextComponent`）。方法分组：
- 内容：`setContentType/getContentType/setText/getText`。
- 编辑器套件：`getEditorKit/setEditorKit/createEditorKitForContentType/getEditorKitForContentType`。
- 超链接：`addHyperlinkListener/removeHyperlinkListener/getHyperlinkListeners`。

## 2. Java 用户如何使用
用户编辑富文本或纯文本：
```java
JEditorPane pane = new JEditorPane("text/html", "<b>hi</b>");
pane.setContentType("text/plain");
```
典型链路：`new JEditorPane(type, text)` → `setText/getText` → `setContentType`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/JEditorPane.java` 是 JDK 自身实现（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JEditorPane.java`）。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `JTextComponent`。
- 行为（黑盒）：`testBlackBox()` 覆盖内容类型、`setText/getText`、`EditorKit`、超链接监听器 add/remove、静态/实例 `createEditorKitForContentType`、typed 构造。
