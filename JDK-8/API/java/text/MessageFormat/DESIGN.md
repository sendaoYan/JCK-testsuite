# java.text.MessageFormat 测试设计案（JDK-8）

> 覆盖目标：完整契约（构造/异常、基本替换、子格式、注入、解析、模式/区域、迭代器、equals/clone、
> 边界、序列化），非冒烟。

## 1. API 提供了哪些功能

- 构造器：`MessageFormat(String)`、`MessageFormat(String,Locale)`。
- 模式/区域：`applyPattern`、`toPattern`、`setLocale`、`getLocale`。
- 子格式注入：`setFormatsByArgumentIndex(Format[])`、`setFormats(Format[])`、`setFormatByArgumentIndex(int,Format)`、
  `setFormat(int,Format)`、`getFormatsByArgumentIndex()`、`getFormats()`。
- 格式化：`format(Object[],StringBuffer,FieldPosition)`、静态 `format(String,Object...)`、`format(Object,...)`、
  `formatToCharacterIterator`。
- 解析：`parse(String,ParsePosition)`、`parse(String)`（抛 ParseException）、`parseObject`。
- 契约：`clone`、`equals`、`hashCode`；`Serializable`。

## 2. Java 用户如何使用

- `new MessageFormat("Hello {0}, you have {1} messages.", Locale.US)` → `format(new Object[]{...})`。
- 子格式：`{0,number,#.##}`、`{0,choice,0#zero|1#one|2#many}`；`setFormatByArgumentIndex` 注入固定
  SimpleDateFormat（UTC）保证日期确定性。

## 3. 参考来源（不凭记忆）

- Oracle Java SE 8 javadoc + `~/git/jdk8u-dev/jdk/src/share/classes/java/text/MessageFormat.java`。
- JDK-27 同目录 DESIGN.md（JDK-8 实测：`parse("")` 返回 `{""}`（长度 1 空串元素），与 JDK-27 一致）。

## 4. 测试用例设计（映射到 MessageFormatTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：extends Format、构造器、setFormatsByArgumentIndex、format 重载、Serializable | testWhiteBox |
| P0 | 基本替换 + 静态 format + format(Object[],StringBuffer,FieldPosition) | testBasicSubstitution |
| P0 | 非法 pattern（未闭合 `{`）→IAE；ctor(null)→NPE | testConstructorsAndExceptions |
| P0 | number/choice 子格式 | testSubformats |
| P0 | setFormatByArgumentIndex/setFormatsByArgumentIndex 注入固定 UTC 日期 | testExplicitFormats |
| P0 | parse 还原 String/Number 类型 + 空串解析 | testParse |
| P0 | applyPattern/toPattern/setLocale/getLocale | testPatternAndLocale |
| P0 | formatToCharacterIterator + equals/clone/hashCode | testIteratorEqualsClone |
| P1 | 边界：空 pattern/缺失参数/Integer.MIN/MAX/Long.MAX | testBoundaryValues |
| P1 | setFormat/setFormats/parseObject | testSetFormatAndParseObject |
| P1 | 序列化 round-trip | testSerialization |

## 关键规范细节（易错，已锁入断言）

- 非法 pattern（未闭合 `{`）抛 `IllegalArgumentException`。
- 缺失参数（如 `{0} {1}` 只传 1 个）保留占位符文本。
- `parse("")` 返回长度 1、含空串元素的数组。

## 注意事项

- 固定 Locale.US + UTC + Date(0)；日期用显式 SimpleDateFormat（不用默认时区 {time} 子格式）；headless-safe 不加 `@key`。
