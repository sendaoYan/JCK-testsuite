# javax.swing.text.html.HTML.Tag — DESIGN

## 1. API 功能
HTML 标签类型（`HTML` 的嵌套 public 类，`extends Object`，非 enum，无接口）。核心方法分组：
- 构造器：`HTML.Tag()`（默认，标志均为 false）。
- 查询：`toString()`（返回标签名）、`isBlock()`、`breaksFlow()`、`isPreformatted()`。
- 常量：A、P、BODY、HTML、BR、PRE、TEXTAREA、DIV 等。

## 2. Java 用户如何使用
通过 `HTML.getTag("a")` 或常量 `HTML.Tag.A` 获得实例；用 `toString()` 取标签名、`isBlock()`/`breaksFlow()`/`isPreformatted()` 判断排版行为。典型链：`HTML.Tag.P.isBlock() && HTML.Tag.P.breaksFlow()`。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/parser/Parser.java:372`：`if (tag.breaksFlow()) { ... }` 判断标签是否断开流。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLWriter.java:363`：`return name.isBlock();` 判断输出时是否按块写。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/HTMLDocument.java:576`：`if (t.isBlock()) { return null; }`（getIterator 用 isBlock 判断）。

## 4. 测试用例设计（黑盒部分，`TagTest.testBlackBox`）
1. toString：A→"a"、P→"p"、BODY→"body"、HTML→"html"。
2. A 非 block/非 break/非 preformatted。
3. P isBlock && breaksFlow && 非 preformatted。
4. BR 非 block 但 breaksFlow。
5. PRE、TEXTAREA isPreformatted；DIV 非 preformatted。
6. `new HTML.Tag()` 默认 isBlock/breaksFlow/isPreformatted 全 false。
7. `HTML.getTag("a")` == A（身份）；A != P。
