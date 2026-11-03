# javax.swing.text.html.CSS — DESIGN

## 1. API 功能
HTML/CSS 常量与属性注册表（`implements Serializable`，非抽象，`extends Object`）。核心方法分组：
- 构造器：`CSS()`。
- 静态查询/工厂：`getAttribute(String)`（按名称返回唯一 `CSS.Attribute` 实例）、`getAllAttributeKeys()`（返回全部 67 个 `CSS.Attribute[]`）。
- 嵌套属性类型 `CSS.Attribute`：常量 COLOR/FONT_SIZE/FONT_FAMILY/BACKGROUND 等，提供 `toString()`、`getDefaultValue()`、`isInherited()`。

## 2. Java 用户如何使用
`new CSS()` 得到工具实例；`CSS.getAttribute("color")` 拿到属性键 → `.getDefaultValue()` / `.isInherited()` 判断默认值与继承性；`CSS.getAllAttributeKeys()` 遍历全部 CSS 键。属性键作为 `AttributeSet` 的 key 存取值（配合 StyleSheet 使用）。典型链：`CSS.getAttribute("font-size").getDefaultValue()`。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/StyleSheet.java:3404`：`CSS.Attribute cssKey = CSS.getAttribute(propertyName);` 把 CSS 属性名解析为属性键。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/InlineView.java:189`：`Object decor = a.getAttribute(CSS.Attribute.TEXT_DECORATION);` 用属性键读取样式值。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/html/StyleSheet.java:558`：`addCSSAttribute(MutableAttributeSet, CSS.Attribute key, ...)` 以 `CSS.Attribute` 为 key 注入样式。

## 4. 测试用例设计（黑盒部分，`CSSTest.testBlackBox`）
1. `new CSS()` 非 null。
2. `getAllAttributeKeys()` 长度 == 67。
3. `getAttribute("color")` → toString "color"、默认值 "black"、isInherited()==true。
4. `getAttribute("font-size")` → "font-size"、"medium"、继承。
5. `getAttribute("font-family")` → "font-family"、默认 null、继承。
6. `getAttribute("background")` → 默认 null、不继承。
7. `getAttribute("no-such-attr")` → null。
8. 常量 COLOR/FONT_SIZE/FONT_FAMILY/BACKGROUND 与 getAttribute 返回同一实例（身份断言）。
