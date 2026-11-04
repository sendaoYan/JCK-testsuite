# javax.swing.text.StyleConstants.FontConstants — DESIGN

## 1. API 功能
`StyleConstants` 的嵌套 final 类，作为“字体”属性键对象的运行时类型（`extends StyleConstants implements AttributeSet.FontAttribute, AttributeSet.CharacterAttribute`）。无 public 构造器。

## 2. Java 用户如何使用
```java
StyleConstants.Bold / Italic / FontFamily / FontSize   // FontConstants 实例，作为键
StyleConstants.setBold(s, true);
StyleConstants.setFontFamily(s, "Serif");
```

## 3. 开源代码实际用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/StyleConstants.java`：`FontFamily = new FontConstants("family")`、`FontSize = new FontConstants("size")`、`Bold = new FontConstants("bold")`、`Italic = new FontConstants("italic")`；`FontConstants` 声明 `implements AttributeSet.FontAttribute, AttributeSet.CharacterAttribute`，构造器 `private FontConstants(String)`。

## 4. 测试用例设计 + 自动化实现（`FontConstantsTest.testBlackBox`）
1. 无 public 构造器；同时实现 FontAttribute 与 CharacterAttribute。
2. `Bold/Italic/FontFamily/FontSize` 是该类实例；`Underline` 不是。
3. 行为闭环：`setBold/setItalic/setFontFamily/setFontSize` 后读回正确值。
