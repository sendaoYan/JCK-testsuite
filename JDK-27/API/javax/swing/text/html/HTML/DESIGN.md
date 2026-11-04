# javax.swing.text.html.HTML — DESIGN

## 1. API 功能
HTML 标签/属性常量与注册表（非抽象，`extends Object`，无接口）。核心方法分组：
- 构造器：`HTML()`。
- 常量：`NULL_ATTRIBUTE_VALUE`（值为 "#DEFAULT"）。
- 静态查询/工厂：`getAllTags()`（全部 74 个 `HTML.Tag[]`）、`getTag(String)`、`getAllAttributeKeys()`（全部 81 个 `HTML.Attribute[]`）、`getAttributeKey(String)`、`getIntegerAttributeValue(AttributeSet, HTML.Attribute, int default)`。
- 嵌套类型：`Tag`、`Attribute`、`UnknownTag`。

## 2. Java 用户如何使用
`new HTML()` 得到工具实例；`HTML.getTag("a")` 拿标签常量、`HTML.getAttributeKey("href")` 拿属性常量；`HTML.getIntegerAttributeValue(attrs, HTML.Attribute.COLSPAN, 5)` 从属性集读整数（缺失/非法时回退默认值）。典型链：`HTML.getIntegerAttributeValue(attr, HTML.Attribute.SIZE, 1)`。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/FormView.java:210`：`int size = HTML.getIntegerAttributeValue(attr, HTML.Attribute.SIZE, 1);` 读表单控件尺寸。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/parser/TagElement.java:59`：`htmlTag = HTML.getTag(elem.getName());` 按元素名解析 HTML 标签。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/parser/Parser.java:1564`：`HTML.Attribute attkey = HTML.getAttributeKey(attname);` 按属性名解析属性键。

## 4. 测试用例设计（黑盒部分，`HTMLTest.testBlackBox`）
1. `new HTML()` 非 null；NULL_ATTRIBUTE_VALUE 等于 "#DEFAULT"。
2. `getAllTags()` 长度 == 74 且包含 A/P/BODY。
3. `getTag("a"/"p"/"body")` 身份等于常量；`getTag("zzz-no-such")` == null。
4. `getAllAttributeKeys()` 长度 == 81 且包含 HREF/SRC/TITLE。
5. `getAttributeKey("href"/"src"/"title")` 身份等于常量；未知属性 == null。
6. `getIntegerAttributeValue`：空属性集回退默认 5；"3" 解析为 3；"abc" 回退默认 5。
