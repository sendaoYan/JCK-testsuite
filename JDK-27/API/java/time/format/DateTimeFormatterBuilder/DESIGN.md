# java.time.format.DateTimeFormatterBuilder

## 1. API 功能
流式构造 `DateTimeFormatter` 的构建器。`final`、公开无参构造器。`DateTimeFormatter.ofPattern` 内部即调用它。

- 字段/文本：`appendValue`、`appendText`、`appendLiteral`
- 偏移/瞬时：`appendOffsetId`、`appendInstant`
- 解析控制：`parseCaseSensitive/Insensitive`、`parseStrict/Lenient`、`parseDefaulting`
- 结构：`appendPattern`、`optionalStart/optionalEnd`、`padNext`
- 产出：`toFormatter()`、`toFormatter(Locale)`；静态 `getLocalizedDateTimePattern(...)`

## 2. Java 用户如何使用
```java
DateTimeFormatter f = new DateTimeFormatterBuilder()
    .appendValue(ChronoField.YEAR)
    .appendLiteral('-')
    .appendValue(ChronoField.MONTH_OF_YEAR, 2)
    .appendLiteral('-')
    .appendValue(ChronoField.DAY_OF_MONTH, 2)
    .toFormatter();
f.format(LocalDate.of(2024,6,1)); // "2024-06-01"
```

## 3. 开源代码实际用法调研（来源：JDK 源码 + test/jdk）
- `jdk25u-dev/src/java.base/share/classes/java/time/format/DateTimeFormatter.java`：第 569 行 `new DateTimeFormatterBuilder().appendPattern(pattern).toFormatter()`（`ofPattern` 实现）。
- `jdk25u-dev/src/java.base/share/classes/java/time/DayOfWeek.java`：第 227 行 `new DateTimeFormatterBuilder().appendText(DAY_OF_WEEK, style).toFormatter(locale)`（`getDisplayName` 实现）。
- `jdk25u-dev/src/java.base/share/classes/java/time/Month.java`：`getDisplayName` 同样用 `appendText`。
- `jdk25u-dev/test/jdk/java/time/tck/java/time/format/TCKDateTimeFormatterBuilder.java`：TCK 覆盖 appendValue/appendText/padNext/optional（权威来源）。

## 4. 测试用例设计 + 自动化实现
- 白盒：反射断言 `final`、非接口/抽象/枚举、父类 `Object`、存在公开无参构造器。
- 黑盒：逐字段组合 `yyyy-MM-dd` 并格式化+解析回环；`appendText(DAY_OF_WEEK, FULL, US)`==Saturday；`appendPattern("yyyy-MM-dd")`；`parseDefaulting(HOUR_OF_DAY, 0)`；`padNext(4)` 补位；`optionalStart/End` 解析 "2024-06"→YearMonth 与 "2024-06-01"→LocalDate；`parseCaseInsensitive` 解析 "jun"→JUNE；`appendOffsetId` 输出 "+02:00"；`appendInstant` 输出 epoch 的 ISO 串；fluent 返回 this。
- 全部固定 `Locale.US`/固定输入、无墙钟、无全局状态、headless-safe。
