# java.awt.font.TextAttribute 设计案

## 1. API 功能分组
- **类型**：`TextAttribute extends AttributedCharacterIterator.Attribute`（final）。
- **属性键**：`FAMILY/WEIGHT/WIDTH/POSTURE/SIZE/TRANSFORM/SUPERSCRIPT/FONT/CHAR_REPLACEMENT/FOREGROUND/BACKGROUND/UNDERLINE/STRIKETHROUGH/RUN_DIRECTION/JUSTIFICATION/KERNING/LIGATURES/TRACKING/NUMERIC_SHAPING/INPUT_METHOD_HIGHLIGHT/INPUT_METHOD_UNDERLINE/SWAP_COLORS/BIDI_EMBEDDING`。
- **属性值常量**：`WEIGHT_*`（Float）、`WIDTH_*`（Float）、`POSTURE_*`（Float）、`SUPERSCRIPT_*`（Integer）、`UNDERLINE_*`（Integer）、`RUN_DIRECTION_*`（Boolean）、`SWAP_COLORS_ON`（Boolean）、`KERNING_ON/LIGATURES_ON`（Integer）、`TRACKING_*`（Float）等。

## 2. Java 用户如何使用
构造带属性的文本：`AttributedString s = new AttributedString("text"); s.addAttribute(TextAttribute.FONT, font); s.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);` 然后交给 TextLayout/TextMeasurer 排版。

## 3. 开源代码实际用法调研
- `java.awt.font.TextLayout` 的构造接受 `Map<? extends Attribute,?>`，用户以 `TextAttribute.*` 键填充（`src/java.desktop/share/classes/java/awt/font/TextLayout.java`）。
- `test/jdk/java/awt/font/TextLayout/` 用例大量用 TextAttribute 键值构造带样式文本（本用例常量值断言参考）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`TextAttributeTest.testBlackBox()`）：
1. 属性键非空且互异、是 TextAttribute 实例。
2. 数值常量精确值（WEIGHT/SUPERSCRIPT/UNDERLINE/POSTURE/KERNING/LIGATURES/RUN_DIRECTION/BOOLEAN）。
3. 值常量类型（Float/Integer/Boolean）；readResolve 保持单例身份。
白盒（`testWhiteBox()`）：public final、extends Attribute、18 个属性键字段存在。
