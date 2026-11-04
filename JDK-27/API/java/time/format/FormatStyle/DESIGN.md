# java.time.format.FormatStyle

## 1. API 功能
本地化日期/时间格式的粗细粒度枚举：`FULL`、`LONG`、`MEDIUM`、`SHORT`。

## 2. Java 用户如何使用
```java
DateTimeFormatter f = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL);
FormatStyle.valueOf("MEDIUM"); // MEDIUM
```

## 3. 开源代码实际用法调研（来源：JDK 源码 + test/jdk）
- `jdk25u-dev/src/java.base/share/classes/java/time/format/DateTimeFormatter.java`：`ofLocalizedDate/Time/DateTime(FormatStyle, ...)` 接受 FormatStyle。
- `jdk25u-dev/src/java.base/share/classes/java/time/format/DateTimeFormatterBuilder.java`：`appendLocalized(FormatStyle, FormatStyle)`。
- `jdk25u-dev/test/jdk/java/time/tck/java/time/format/TCKFormatStyle.java`：TCK 覆盖各 style 的本地化模式（权威来源）。
- `jdk25u-dev/test/jdk/java/time/tck/java/time/format/TCKLocalizedPrinterParser.java`：本地化格式化行为。

## 4. 测试用例设计 + 自动化实现
- 白盒：反射断言 `isEnum`、`final`、父类 `Enum`、`getEnumConstants().length == 4`。
- 黑盒：`values().length == 4`、四个 `valueOf`；`DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL/SHORT)` 非空且 `getLocale()` 非空（不断言本地化文本，避免 locale 数据差异）。
- 全部固定输入、headless-safe。
