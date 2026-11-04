# javax.swing.text.html.HTML — DESIGN

## 1. API 功能
HTML 标签/属性常量与注册表（非抽象，`extends Object`，无接口）。核心方法分组：
- 构造器：`HTML()`；常量：`NULL_ATTRIBUTE_VALUE`（"#DEFAULT"）。
- 静态查询/工厂：`getAllTags()`（全部 74 个）、`getTag(String)`、`getAllAttributeKeys()`（全部 81 个）、`getAttributeKey(String)`、`getIntegerAttributeValue(AttributeSet, HTML.Attribute, int)`。
- 嵌套类型：`Tag`（isBlock/isPreformatted/breaksFlow）、`Attribute`、`UnknownTag`。

## 2. Java 用户如何使用
`HTML.getTag("a")` 拿标签常量、`HTML.getAttributeKey("href")` 拿属性常量；`HTML.getIntegerAttributeValue(attrs, HTML.Attribute.SIZE, 1)` 从属性集读整数（缺失/非法回退默认值）。属性键在元素 AttributeSet 中以 HTML.Attribute 为 key 存取（如 ENDTAG 键的值是 String "true"）。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/html/FormView.java`：`int size = HTML.getIntegerAttributeValue(attr, HTML.Attribute.SIZE, 1);`。
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/html/parser/TagElement.java`：`htmlTag = HTML.getTag(elem.getName());`。
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/html/parser/Parser.java`：`HTML.Attribute attkey = HTML.getAttributeKey(attname);`。

## 4. 测试用例设计（黑盒，`HTMLTest`：testShape/testTags/testUnknownTag/testAttributes/testIntegerParsing，663 断言）
1. testShape：类 shape + 方法存在性（getAllTags/getTag/getAllAttributeKeys/getAttributeKey/getIntegerAttributeValue）。
2. testTags：getAllTags 长度 74；遍历断言每个标签 toString 非空、isBlock/isPreformatted/breaksFlow 一致性、且出现在 getAllTags 返回集中；getTag(已知名) 身份等于返回集中实例；getTag(unknown) null；getTag(null) NPE（精确异常）；HTML.Tag.HTML toString。
3. testUnknownTag：`new UnknownTag("x")` 的 equals/hashCode/toString 语义、非预定义标签行为。
4. testAttributes：getAllAttributeKeys 长度 81；遍历每个属性 toString/getDefaultValue；getAttributeKey(已知) 身份、未知 null。
5. testIntegerParsing：空属性集回退默认、"3" 解析 3、"abc" 回退默认。
