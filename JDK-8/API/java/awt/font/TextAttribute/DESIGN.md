# java.awt.font.TextAttribute 测试设计

## 1. API 功能
文本属性键类（extends AttributedCharacterIterator.Attribute，public final）：
23 个属性键常量（FAMILY/SIZE/WEIGHT/POSTURE/SUPERSCRIPT/FOREGROUND/...）与约 35 个值常量
（WEIGHT_BOLD、UNDERLINE_ON、KERNING_ON、LIGATURES_ON、RUN_DIRECTION_* 等）。

## 2. Java 用户如何使用
`Map<TextAttribute,Object> m = new HashMap<>(); m.put(TextAttribute.FAMILY, "Dialog");
m.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD); Font f = Font.getFont(m);`
TextLayout/AttributedString 同样以这些键携带样式。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/font/TextAttribute.java`；
Font.deriveFont/TextLayout/AttributedString 调用点；`test/jdk/java/awt/font/`。

## 4. 测试用例设计 + 易错细节
- 全部 23 个键逐一断言 toString() 精确字符串（"family"/"size"/"weight"/"bidi_embedding" 等）与互异性。
- 值常量精确值/类型：WEIGHT_BOLD=2.0f(Float)、WIDTH_SEMI_EXTENDED=1.25f/WIDTH_EXTENDED=1.5f（易错：
  不是 1.125/1.25）、SUPERSCRIPT_SUB=-1/SUPER=1(Integer)、UNDERLINE_ON=0(Integer)、
  KERNING_ON/LIGATURES_ON=1、RUN_DIRECTION_LTR=Boolean.TRUE、INPUT_METHOD_* 语义。
- 键序列化：round-trip 后与常量 **identity 相等**（readResolve 单例），equals/hashCode 稳定。
- 形态：public final、protected 构造器、extends Attribute、Serializable。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动。
