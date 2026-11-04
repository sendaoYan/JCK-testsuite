# javax.swing.text.html.parser.TagElement — DESIGN

## 1. API 功能
通用 HTML TagElement，定义标签周围空白解释方式。内部持有 `Element elem`、`HTML.Tag htmlTag`、`boolean insertedByErrorRecovery`。
- 构造器：`TagElement(Element elem)`、`TagElement(Element elem, boolean fictional)`。
- 查询：`breaksFlow()/isPreformatted()/getElement()/getHTMLTag()/fictional()`。

## 2. Java 用户如何使用
解析器回调 `handleStartTag(TagElement)` 等中拿到 TagElement，读 `getElement()`（DTD 元素）、`getHTMLTag()`（HTML.Tag 常量或 UnknownTag）、`breaksFlow()/isPreformatted()` 决定空白处理、`fictional()` 判断是否为错误恢复插入。

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/parser/TagElement.java`（自用）：
  ```java
  public TagElement(Element elem, boolean fictional) {
      this.elem = elem;
      htmlTag = HTML.getTag(elem.getName());
      if (htmlTag == null) htmlTag = new HTML.UnknownTag(elem.getName());
      insertedByErrorRecovery = fictional;
  }
  ```
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/parser/DocumentParser.java`（自用）：`Element elem = tag.getElement(); ... if (tag.fictional()) { ... } else { callback.handleStartTag(tag.getHTMLTag(), ...); }`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/parser/Parser.java`（自用）：`makeTag(Element elem, boolean fictional)` 包装为 `new TagElement(elem, fictional)`。

## 4. 测试用例设计（黑盒部分，`TagElementTest.testBlackBox`）
1. body：getElement 往返、getHTMLTag==HTML.Tag.BODY、fictional=false、breaksFlow=true、isPreformatted=false。
2. 双参构造器 fictional=true。
3. 块级 p/pre breaksFlow、内联 b/img 不 breaksFlow、pre isPreformatted。
4. 未知元素 → HTML.UnknownTag、非 fictional、getElement().getName() 往返。
