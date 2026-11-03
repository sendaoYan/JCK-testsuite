# java.text.AttributedCharacterIterator.Attribute — 测试设计案

## 1. API 功能
属性文本的**属性键**类型：定义一段 run 上附着的属性种类。

- 常量：`LANGUAGE`/`READING`/`INPUT_METHOD_SEGMENT`（`public static final`，值由各自的 name 决定）。
- 构造：`protected Attribute(String name)`（供具体 Format 子类/框架定义新属性，如
  `TextAttribute`、`NumberFormat.Field` 均继承自此类）。
- 协议：`final equals/hashCode`（**身份语义**——直接委托 Object.equals，常量因单例而自等；不同实例
  即使同名也不相等）、`toString`（返回 `类名(name)`）、`protected getName()`、
  `protected readResolve()`（反序列化时回归单例）、implements Serializable。

## 2. Java 用户如何使用
作为 `AttributedString.addAttribute(attr, value, begin, end)` 的键，或从
`AttributedCharacterIterator.getAttribute(attr)` 取值；框架则继承 Attribute 定义领域属性。
链路：拿常量（LANGUAGE）-> addAttribute 标记 run -> iterate -> getAttribute(LANGUAGE) 取回值；
或自定义 `class MyAttr extends Attribute { MyAttr(String n){super(n);} }` 用同名相等性做 Map 键。

## 3. 开源代码实际用法调研
- OpenJDK `src/java.base/share/classes/java/text/AttributedCharacterIterator.java` 的嵌套类
  Attribute（本地 `~/git/jdk25u-dev`）：`LANGUAGE = new Attribute("language")`、
  `READING = new Attribute("reading")`、`INPUT_METHOD_SEGMENT = new Attribute("input_method_segment")`，
  `equals` 判定 `instanceof Attribute && getName().equals(other.getName())`——第 2/3/4 节对齐。
- OpenJDK `src/java.desktop/share/classes/java/awt/font/TextAttribute.java`：`TextAttribute extends
  Attribute` 并定义大量常量——是“继承 Attribute 自定义属性”的权威范例。
- OpenJDK `src/java.base/share/classes/java/text/NumberFormat.java` 的 `NumberFormat.Field extends
  Format.Field`，而 Format.Field 间接与 Attribute 同源——格式化器字段属性的同类用法。
- Oracle javadoc
  (https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/text/AttributedCharacterIterator.Attribute.html)。

## 4. 测试用例设计 + 自动化实现（AttributeTest.java 黑盒部分）
| 场景 | 断言 |
|---|---|
| 常量形态 | 三常量非空、两两不同 |
| equals/hashCode | 自反、同常量相等（单例身份）、hashCode 一致、null/异型 false |
| toString | 类名(language)/类名(reading)/类名(input_method_segment)（contains 断言） |
| 受保护构造+getName | 自定义子类 toString 含 name；同名自定义实例身份不等、与常量不等 |
| 键/选择器 | 常量作 HashMap 键可跨等值实例命中；作 AttributedString run 选择器可标定属性 |

共 5 组场景、约 18 个 check（`testBlackBox()`）。
