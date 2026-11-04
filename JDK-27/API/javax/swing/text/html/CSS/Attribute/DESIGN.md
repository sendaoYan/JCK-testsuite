# javax.swing.text.html.CSS.Attribute — DESIGN

## 1. API 功能
CSS 属性键类型（`CSS` 的嵌套 final 类，`extends Object`，非 enum，无接口）。核心方法分组：
- 实例方法：`toString()`（返回属性名，如 "color"）、`getDefaultValue()`（返回该属性的默认值，可为 null）、`isInherited()`（是否继承）。
- 常量：COLOR、BACKGROUND、FONT_FAMILY、FONT_SIZE 等（由 `CSS.getAllAttributeKeys()` 统一登记）。

## 2. Java 用户如何使用
通过 `CSS.getAttribute("color")` 或 `CSS.getAllAttributeKeys()` 获取唯一实例；用 `toString()` 取属性名、`getDefaultValue()` 取默认值、`isInherited()` 判断继承。属性实例作为 `AttributeSet` 的 key 用于样式读写。典型链：`CSS.getAttribute("color").getDefaultValue()`。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/StyleSheet.java:558`：`addCSSAttribute(MutableAttributeSet attr, CSS.Attribute key, ...)` 以 `CSS.Attribute` 常量作为样式键写入。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/InlineView.java:189`：`Object decor = a.getAttribute(CSS.Attribute.TEXT_DECORATION);` 用常量读样式值。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/StyleSheet.java:959`：`Color c = css.getColor(a, CSS.Attribute.COLOR);` 按 COLOR 键取颜色。

## 4. 测试用例设计（黑盒部分，`AttributeTest.testBlackBox`）
1. 常量 toString：COLOR→"color"、BACKGROUND→"background"、FONT_FAMILY→"font-family"、FONT_SIZE→"font-size"。
2. COLOR 默认值 "black" 且 isInherited()==true；BACKGROUND 默认 null 且不继承；FONT_FAMILY 默认 null。
3. COLOR != FONT_SIZE（常量互异）。
4. `CSS.getAttribute("color"/"font-size")` 返回与常量相同的实例（身份断言）。
5. `CSS.getAllAttributeKeys()` 遍历包含 COLOR。
