# javax.swing.text.AttributeSet.FontAttribute — DESIGN

## 1. API 功能
`AttributeSet` 的嵌套空标记接口，用于给“字体呈现”相关的属性键打类型标签。无任何方法。

## 2. Java 用户如何使用
用户以 `instanceof AttributeSet.FontAttribute` 判断属性键是否影响字体：
```java
if (key instanceof AttributeSet.FontAttribute) { /* 字体样式 */ }
```

## 3. 开源代码实际用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/AttributeSet.java`：`public interface FontAttribute {}`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/StyleConstants.java`：`FontFamily = new FontConstants("family")`、`FontSize = new FontConstants("size")`、`Bold = new FontConstants("bold")`、`Italic = new FontConstants("italic")`；`FontConstants` 同时 `implements FontAttribute, CharacterAttribute`。

## 4. 测试用例设计 + 自动化实现（`FontAttributeTest.testBlackBox`）
1. 接口无方法、无字段（空标记）。
2. `StyleConstants$FontConstants` 同时实现 `FontAttribute` 与 `CharacterAttribute`。
3. `Bold/Italic/FontFamily/FontSize` 是 FontAttribute 实例（且 Bold 是 CharacterAttribute）；`Underline` 不是 FontAttribute。
4. `SimpleAttributeSet` 不实现该标记。
5. 行为闭环：`setBold/setFontFamily/setFontSize` 后 `isBold/getFontFamily/getFontSize` 正确读回。
