# javax.swing.text.html.CSS — DESIGN

## 1. API 功能
HTML/CSS 常量与属性注册表（`implements Serializable`，非抽象，`extends Object`）。核心方法分组：
- 构造器：`CSS()`。
- 静态查询/工厂：`getAttribute(String)`（按名称返回唯一 `CSS.Attribute` 实例，缓存命中返回同一实例）、`getAllAttributeKeys()`（全部 67 个）。
- 嵌套属性类型 `CSS.Attribute`：常量 COLOR/FONT_SIZE/FONT_FAMILY/BACKGROUND 等，提供 `toString()`、`getDefaultValue()`、`isInherited()`。

## 2. Java 用户如何使用
`CSS.getAttribute("color")` 拿属性键 → `.getDefaultValue()`/`.isInherited()` 判断默认值与继承性；属性键作为 AttributeSet 的 key 配合 StyleSheet 使用；`stringToColor` 换算颜色字符串（存在 JDK-8 与 JDK-9+ 的已知解析差异，需按版本分支断言）。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/html/StyleSheet.java`：`CSS.Attribute cssKey = CSS.getAttribute(propertyName);` 把 CSS 属性名解析为属性键。
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/html/InlineView.java`：`Object decor = a.getAttribute(CSS.Attribute.TEXT_DECORATION);`。
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/html/StyleSheet.java`：`addCSSAttribute(MutableAttributeSet, CSS.Attribute key, ...)`。

## 4. 测试用例设计（黑盒，`CSSTest`：testShape/testAttributeFactory/testDefaultsAndInheritance/testSerialization，160 断言）
1. testShape：类 shape + 公有/保护修饰符 + 唯一接口 Serializable + 方法存在性。
2. testAttributeFactory：getAllAttributeKeys 长度 67；getAttribute("color"/"font-size"/"background-color"/"list-style-type") 缓存身份（同一实例）；未知名 null；getAttribute(null) NPE（精确异常）；查询稳定性。
3. testDefaultsAndInheritance：每个属性键的 toString/getDefaultValue/isInherited 逐一断言（含 color=black 继承、font-size=medium 继承、font-family 默认 null 继承、background 默认 null 不继承等）。
4. 版本差异（javap 双侧对照后各自断言）：JDK-8 `stringToColor("#F00")` 为错误值 Color(0,15,0)、JDK-27 为 red；`stringToColor("123")` JDK-8 (0,1,35) vs JDK-27 (17,34,51)；background-position 默认 null（J27 "0% 0%"）。
5. testSerialization：serialVersionUID 双侧不同值各自断言。
