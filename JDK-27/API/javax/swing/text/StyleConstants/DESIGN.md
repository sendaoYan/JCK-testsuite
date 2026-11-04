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

## 5. 契约深化（本轮追加）
`testElementNames/testKeyToString/testAlignmentConstants/testDefaults/testCharacterRoundTrips/testParagraphRoundTrips/testComponentAndIcon`（88 断言）：
- 元素名常量精确串：ComponentElementName="component"、IconElementName="icon"。
- 全部键常量 toString 精确（"name"/"resolver"/"family"/"size"/"bold"…"TabSet"/"Orientation"）；Family/Size 是 FontFamily/FontSize 的同一对象别名。
- 对齐常量精确值：ALIGN_LEFT=0/ALIGN_CENTER=1/ALIGN_RIGHT=2/ALIGN_JUSTIFIED=3。
- 空 set 默认值：bidiLevel 0、family "Monospaced"、size 12、各布尔 false、foreground/background black、各缩进/间距 0f、alignment LEFT、tabSet/component/icon null。
- 字符面 round-trip：bidi(存 Integer)/family/bold(存 Boolean)/italic/underline/strikeThrough/superscript/subscript/size/foreground/background；set false 后属性仍在（isDefined true 但值 false）；极端 size（MAX_VALUE/0/负数）原样存储不抛异常。
- 段落面：FirstLineIndent/LeftIndent(负值保留)/RightIndent/LineSpacing/SpaceAbove/SpaceBelow/Alignment/TabSet（identity 存取）。
- ComponentAttribute/IconAttribute：set/get 同一实例往返。
