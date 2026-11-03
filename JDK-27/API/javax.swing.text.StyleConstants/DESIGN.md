# javax.swing.text.StyleConstants — DESIGN

## 1. API 功能
样式属性键常量的容器类 + 一组静态读写访问器（作用于 `AttributeSet`/`MutableAttributeSet`）。核心分组：
- 键常量：`FontFamily/Family`、`FontSize/Size`、`Bold/Italic/Underline/StrikeThrough/Superscript/Subscript`、`Foreground/Background`、`FirstLineIndent/LeftIndent/RightIndent/LineSpacing/SpaceAbove/SpaceBelow/Alignment/TabSet`、`BidiLevel`、`ComponentAttribute/IconAttribute/ComposedTextAttribute/ModelAttribute/NameAttribute/ResolveAttribute`。
- 对齐常量：`ALIGN_LEFT=0`、`ALIGN_CENTER=1`、`ALIGN_RIGHT=2`、`ALIGN_JUSTIFIED=3`。
- 元素名常量：`ComponentElementName="component"`、`IconElementName="icon"`。
- 访问器：`setXxx(MutableAttributeSet, v)` / `getXxx/isXxx(AttributeSet)`。

## 2. Java 用户如何使用
```java
StyleConstants.setBold(s, true);
StyleConstants.setFontFamily(s, "Serif");
StyleConstants.setForeground(s, Color.red);
boolean b = StyleConstants.isBold(s);
int align = StyleConstants.getAlignment(s);
```
默认值：fontFamily "Monospaced"、fontSize 12、foreground/background Color.black、alignment ALIGN_LEFT、bidiLevel 0、各 float 0。

## 3. 开源代码实际用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/StyleConstants.java`：各访问器默认值逻辑（如 `getFontFamily` 回退 "Monospaced"、`getFontSize` 回退 12、`getAlignment` 回退 ALIGN_LEFT）。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/rtf/RTFWriteParagraphAlignTest.java`：`test(StyleConstants.ALIGN_LEFT/CENTER/RIGHT/JUSTIFIED)`。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/bug4148489.java`：`StyleConstants.setFirstLineIndent(sty, -50); StyleConstants.setLeftIndent(sty, 50);`。

## 4. 测试用例设计 + 自动化实现（`StyleConstantsTest.testBlackBox`）
1. 对齐/元素名/别名常量的精确值断言。
2. 空集合上的全部默认值断言。
3. 字符/颜色/段落访问器的 set→get 全量 round-trip（含 setBold(false)、TabSet 恒等）。
