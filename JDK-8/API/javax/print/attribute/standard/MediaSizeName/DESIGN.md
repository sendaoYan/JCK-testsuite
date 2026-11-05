# javax.print.attribute.standard.MediaSizeName 黑盒行为测试设计

## 1. API 功能
EnumSyntax 枚举类（Media 子类），实现 Attribute。常量 ISO_A0, ISO_A1, ISO_A2, ISO_A3, ISO_A4, ISO_A5, ISO_A6, ISO_A7, ISO_A8, ISO_A9, ISO_A10, ISO_B0, ISO_B1, ISO_B2, ISO_B3, ISO_B4, ISO_B5, ISO_B6, ISO_B7, ISO_B8, ISO_B9, ISO_B10, JIS_B0, JIS_B1, JIS_B2, JIS_B3, JIS_B4, JIS_B5, JIS_B6, JIS_B7, JIS_B8, JIS_B9, JIS_B10, ISO_C0, ISO_C1, ISO_C2, ISO_C3, ISO_C4, ISO_C5, ISO_C6, NA_LETTER, NA_LEGAL, EXECUTIVE, LEDGER, TABLOID, INVOICE, FOLIO, QUARTO, JAPANESE_POSTCARD, JAPANESE_DOUBLE_POSTCARD, A, B, C, D, E, ISO_DESIGNATED_LONG, ITALY_ENVELOPE, MONARCH_ENVELOPE, PERSONAL_ENVELOPE, NA_NUMBER_9_ENVELOPE, NA_NUMBER_10_ENVELOPE, NA_NUMBER_11_ENVELOPE, NA_NUMBER_12_ENVELOPE, NA_NUMBER_14_ENVELOPE, NA_6X9_ENVELOPE, NA_7X9_ENVELOPE, NA_9X11_ENVELOPE, NA_9X12_ENVELOPE, NA_10X13_ENVELOPE, NA_10X14_ENVELOPE, NA_10X15_ENVELOPE, NA_5X7, NA_8X10。getName 为 "media"，getCategory 为 Media.class。toString 返回小写连字符名，getValue 返回枚举整数值，clone 返回单例自身，hashCode 等于 value。

## 2. Java 用户如何使用
直接引用静态常量（如 MediaSizeName.ISO_A0），配合 HashAttributeSet 作为打印请求/作业属性。getValue() 得到 IPP 枚举值，toString() 得到标准字符串名。

## 3. 开源代码实际用法调研
实现权威 ~/git/jdk25u-dev/src/java.desktop/share/classes/javax/print/attribute/standard/MediaSizeName.java；test/jdk 打印属性测试引用常量（如 javax/print 属性类）。

## 4. 测试用例设计 + 自动化实现
黑盒：每个常量 getValue/toString 精确值、getCategory/getName 精确字符串、clone 单例、hashCode==value、equals 身份语义、getEnumValueTable/getStringTable/getOffset 反射校验。白盒：public 类、extends Media、implements 各接口。

