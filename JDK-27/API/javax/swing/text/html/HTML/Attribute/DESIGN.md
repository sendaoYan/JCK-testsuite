# javax.swing.text.html.HTML.Attribute — DESIGN

## 1. API 功能
HTML 属性键类型（`HTML` 的嵌套 final 类，`extends Object`，非 enum，无接口）。核心方法分组：
- 实例方法：`toString()`（返回属性名，如 "href"）。
- 常量：HREF、SRC、TITLE、ACTION、METHOD 等（由 `HTML.getAllAttributeKeys()` 统一登记）。

## 2. Java 用户如何使用
通过 `HTML.getAttributeKey("href")` 或 `HTML.getAllAttributeKeys()` 获取唯一实例；用 `toString()` 取属性名。属性实例作为 `AttributeSet` 的 key 用于读写 HTML 元素属性。典型链：`HTML.getAttributeKey("href").toString()` 或 `attrs.getAttribute(HTML.Attribute.HREF)`。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/parser/Parser.java:1564`：`HTML.Attribute attkey = HTML.getAttributeKey(attname);` 按属性名解析属性键。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/FormView.java:210`：`HTML.getIntegerAttributeValue(attr, HTML.Attribute.SIZE, 1)` 用常量作为 key 读取。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLWriter.java`：序列化时用 `HTML.Attribute` 常量取各属性值。

## 4. 测试用例设计（黑盒部分，`AttributeTest.testBlackBox`）
1. 常量 toString：HREF→"href"、SRC→"src"、TITLE→"title"、ACTION→"action"、METHOD→"method"。
2. HREF != SRC（常量互异）。
3. `HTML.getAttributeKey("href"/"src"/"title")` 返回与常量相同实例（身份断言）。
4. `HTML.getAttributeKey(HREF.toString())` == HREF（toString 往返身份）。
5. `HTML.getAllAttributeKeys()` 遍历包含 HREF/SRC/TITLE。
