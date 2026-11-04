# java.time.format.TextStyle

## 1. API 功能
日期时间文本（月/星期）样式枚举，含 standalone 变体：

- `FULL`/`SHORT`/`NARROW`：普通样式
- `FULL_STANDALONE`/`SHORT_STANDALONE`/`NARROW_STANDALONE`：独立样式

方法：`isStandalone()`、`asStandalone()`、`asNormal()`、`values()`、`valueOf(String)`。

## 2. Java 用户如何使用
```java
Month.JANUARY.getDisplayName(TextStyle.SHORT, Locale.US); // "Jan"
DayOfWeek.MONDAY.getDisplayName(TextStyle.FULL, Locale.US); // "Monday"
TextStyle.FULL.asStandalone(); // FULL_STANDALONE
```

## 3. 开源代码实际用法调研（来源：JDK 源码 + test/jdk）
- `jdk25u-dev/src/java.base/share/classes/java/time/DayOfWeek.java`：`getDisplayName(TextStyle, Locale)` 用 `appendText(..., style)`。
- `jdk25u-dev/src/java.base/share/classes/java/time/Month.java`：同样按 TextStyle 输出月份名。
- `jdk25u-dev/src/java.base/share/classes/java/time/format/DateTimeFormatterBuilder.java`：`appendText`/`appendZoneText` 接受 TextStyle。
- `jdk25u-dev/test/jdk/java/time/tck/java/time/format/TCKDateTimeTextPrinting.java`：覆盖文本打印样式（权威来源）。

## 4. 测试用例设计 + 自动化实现
- 白盒：反射断言 `isEnum`、`final`、父类 `Enum`、`getEnumConstants().length == 6`。
- 黑盒：`values().length == 6`、各 `valueOf`；六个常量的 `isStandalone()`；`asStandalone`/`asNormal` 转换；`Month.JANUARY` 的 SHORT/FULL/NARROW（US）== "Jan"/"January"/"J"；`DayOfWeek.MONDAY` 的 FULL/SHORT（US）== "Monday"/"Mon"。
- 全部固定 `Locale.US`（稳定 CLDR/root 数据）、headless-safe。
