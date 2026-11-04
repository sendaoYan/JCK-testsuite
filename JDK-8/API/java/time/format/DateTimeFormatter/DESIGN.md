# java.time.format.DateTimeFormatter 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
- 常量：`ISO_LOCAL_DATE/ISO_LOCAL_TIME/ISO_LOCAL_DATE_TIME/ISO_ZONED_DATE_TIME/ISO_INSTANT/BASIC_ISO_DATE/
  ISO_ORDINAL_DATE/ISO_WEEK_DATE/RFC_1123_DATE_TIME/ISO_DATE/ISO_TIME/ISO_OFFSET_DATE/ISO_OFFSET_TIME/
  ISO_OFFSET_DATE_TIME/ISO_DATE_TIME`。
- 工厂：`ofPattern(String[,Locale])`、`ofLocalizedDate/ofLocalizedTime/ofLocalizedDateTime(FormatStyle[,FormatStyle])`。
- 定制：`withLocale/withZone/withResolverStyle/withChronology/withDecimalStyle/withResolverFields`。
- 查询：`getLocale/getZone/getResolverStyle/getChronology/getDecimalStyle/getResolverFields`。
- 格式化：`format(TemporalAccessor)`、`formatTo(TemporalAccessor,Appendable)`。
- 解析：`parse(CharSequence[,TemporalQuery])`、`parse(CharSequence,ParsePosition)`、`parseBest`、`parseUnresolved`。
- 其它：`toFormat([TemporalQuery])`、`parsedExcessDays()/parsedLeapSecond()`、`toString`。
- 不可序列化（无 serialVersionUID）。

## 2. Java 用户如何使用
```java
DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");
LocalDate d = LocalDate.parse("2024-03-01", f);   // 解析
String s = f.format(d);                            // 格式化
```

## 3. 开源代码实际用法调研
- `~/git/jdk8u-dev/jdk/test/java/time/tck/java/time/format/TCKDateTimeFormatter.java`（ofPattern/parse/withZone 契约）。
- `~/git/jdk8u-dev/jdk/src/share/classes/java/time/format/DateTimeFormatter.java`（ParsePosition 语义）。

## 4. 测试用例设计
- ISO 常量：各常量 format 输出（含 ISO_WEEK_DATE/ISO_ORDINAL_DATE/ISO_INSTANT）。
- ofPattern：带 Locale、默认 pattern、parse round-trip、withZone 格式化 Instant。
- with* 方法：withLocale/withZone/withResolverStyle/withChronology/withDecimalStyle/withResolverFields + getters；默认 resolver SMART。
- format/formatTo：format/formatTo Appendable。
- parse：parse(TemporalQuery)、parse(CharSequence) accessor query、parse(TemporalQuery)、ParsePosition（可解析前缀 index + 无 error）、parseBest、parseUnresolved。
- toFormat：toFormat()/toFormat(query)。
- localized 工厂：ofLocalizedDate/ofLocalizedTime/ofLocalizedDateTime。
- parsedExcessDays/parsedLeapSecond。
- 异常：parse 垃圾/错格式/非法日期→DateTimeParseException；format 缺字段类型→DateTimeException；
  ofPattern(null)/ofPattern(null locale)/format(null)/parse(null)/withLocale(null)→NPE。
- 注：JDK-8 中 `parse(bad, ParsePosition)` 对完全非法输入抛 DateTimeParseException；`withZone(null)` 不抛 NPE。
- 确定性（固定 Locale.US）；headless-safe（无 `@key`）。
