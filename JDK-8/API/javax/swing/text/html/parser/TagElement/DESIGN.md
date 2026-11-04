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

## 5. 深化批（契约覆盖增量）
1. null 契约：两个构造器对 null 元素均 NPE（构造即取 elem.getName()）。
2. html32 全量空白语义矩阵（23 元素 breaksFlow/isPreformatted 精确表）：p/br/head/li/dl/pre/blockquote/hr/dd/dt/ul/ol/h1/center/td 断流；b/img/tr/area/basefont/table/option/address 不断流；仅 pre 预格式化。
3. getHTMLTag 返回 HTML.Tag **单例**（==Tag.BODY/PRE/P/BR/HTML 身份断言）、同实例多次调用稳定；未知元素 -> 新建 HTML.UnknownTag（toString==元素名、不断流不预格式化）。
4. fictional 标志只来自第二构造器，不影响 breaksFlow；TagElement 非 Serializable。

## 版本注记（JDK-8 侧）
- 与 JDK-27 的公开 API 逐项一致（javap -public 双侧比对）；本包已知可观察差异仅 Element.serialVersionUID（见 Element/DESIGN.md），另有 NPE/CCE 异常消息形态差异（JDK-14+ helpful NPE），测试一律只断言异常类型。
