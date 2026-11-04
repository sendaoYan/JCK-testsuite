# java.time.format.DateTimeFormatter 测试方案

## 1. API 功能（依据 JDK-27 `javap`）
- 静态工厂：`ofPattern(String)`、`ofPattern(String,Locale)`、`ofLocalizedDate/ofLocalizedTime/ofLocalizedDateTime(FormatStyle[,Locale])`、`withLocale(Locale)`、`withZone(ZoneId)`、`withChronology/withResolverStyle/withResolverFields/withDecimalStyle`。
- 格式化/解析：`format(TemporalAccessor)`、`parse(CharSequence[,ParsePosition])`、`parseBest/parseUnresolved`、`toFormat()`、`getLocale/getZone/getChronology/getResolverStyle/getDecimalStyle`。
- 常量：`ISO_LOCAL_DATE/ISO_LOCAL_TIME/ISO_LOCAL_DATE_TIME/ISO_OFFSET_DATE_TIME/ISO_ZONED_DATE_TIME/ISO_INSTANT/BASIC_ISO_DATE/ISO_DATE/ISO_TIME/ISO_ORDINAL_DATE/ISO_WEEK_DATE/RFC_1123_DATE_TIME`。

## 2. Java 用户如何使用
```java
DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
String s = LocalDate.of(2024, 3, 1).format(f);          // "2024-03-01"
TemporalAccessor t = f.parse("2024-03-01");              // 解析
LocalDate d = LocalDate.parse("2024-03-01", DateTimeFormatter.ISO_LOCAL_DATE);
String iso = LocalDateTime.now(Clock.fixed(..., ZoneOffset.UTC)).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
```

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/jdk/java/time/test/java/time/format/TestDateTimeFormatterBuilder.java` 及 `TestDateTimeFormatter.java`（ofPattern/ISO_* 常量/parse 契约）。
- `~/git/jdk25u-dev/src/java.base/share/classes/java/time/format/DateTimeFormatter.java`（`ofPattern(String)` 使用 `Locale.getDefault(Locale.Category.FORMAT)`）。

## 4. 测试用例设计 + 自动化实现
对应 `DateTimeFormatterTest.java` 的 `testBlackBox()`：
- ofPattern 自定义格式、ISO_* 常量格式/解析往返、parse 得到 TemporalAccessor 再 LocalDate.from、withLocale/withZone、ofLocalizedDate(Locale.US)、非法 pattern 抛 IllegalArgumentException、非法输入抛 DateTimeParseException。
- 确定性：固定 Locale.US/ROOT + 固定 ZoneId（UTC）+ 固定时刻；注意 `ofPattern(String)` 依赖 `Locale.Category.FORMAT` 默认值，断言据此调整；headless-safe。
- 补充 null 语义矩阵：`ofPattern(null)`/`ofPattern("yyyy", null)`/`format(null)`/`parse(null)`/`withLocale(null)` 均抛 NullPointerException；`withZone(null)` 不抛且返回非 null formatter。
- 补充 ParsePosition 前缀解析：`parse("2024-03-01junk", pos)` 后 `pos.getIndex()==10`、`errorIndex==-1`；坏输入 `parse("bad", pos)` 抛 DateTimeParseException（JDK-27 实测与 JDK-8 一致）。
