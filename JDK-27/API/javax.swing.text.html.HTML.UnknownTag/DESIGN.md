# javax.swing.text.html.HTML.UnknownTag — DESIGN

## 1. API 功能
未知 HTML 标签类型（`HTML` 的嵌套 public 类，`extends HTML.Tag implements Serializable`，非抽象）。核心方法分组：
- 构造器：`UnknownTag(String id)`。
- 查询：`toString()`（返回 id）、继承的 `isBlock()`/`breaksFlow()`/`isPreformatted()`。
- 值语义：`equals(Object)`、`hashCode()`（按 id 比较）。

## 2. Java 用户如何使用
`new HTML.UnknownTag("mycustom")` 构造未知标签，用 `toString()` 取 id、`equals/hashCode` 比较同 id 标签、`instanceof HTML.Tag` 兼容既有代码。典型链：`new HTML.UnknownTag("x").toString()`。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HiddenTagView.java:246`：`new HTML.UnknownTag(text)` 构造未知标签视图。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLEditorKit.java:538`：`else if (tag instanceof HTML.UnknownTag)` 判断未知标签。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLDocument.java:2869`：`getPreservesUnknownTags()` 控制未知标签是否保留。

## 4. 测试用例设计（黑盒部分，`UnknownTagTest.testBlackBox`）
1. `new UnknownTag("mycustom")` 非 null、toString()=="mycustom"、instanceof Tag。
2. isBlock/breaksFlow/isPreformatted 全 false。
3. 同 id 的 `equals`==true、`hashCode` 相等；不同 id 不 equals。
4. 与 String "mycustom" 不 equals、与 null 不 equals。
