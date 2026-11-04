# java.text.DateFormat 测试设计案（JDK-8）

> 覆盖目标：完整契约（抽象层次、18 个字段常量、工厂方法、确定性 format 字符串、parse/lenient、
> equals/clone/hashCode、受保护构造器子类、parseObject），非冒烟。

## 1. API 提供了哪些功能

- 常量：`ERA_FIELD`…`TIMEZONE_FIELD`（字段 id）、`FULL/LONG/MEDIUM/SHORT/DEFAULT`（风格）。
- 工厂：`getDateInstance/getTimeInstance/getDateTimeInstance/getInstance`（各含 style 与 Locale 重载）、
  `getAvailableLocales()`。
- 格式化：`format(Date)`、抽象 `format(Date,StringBuffer,FieldPosition)`。
- 解析：`parse(String)`、抽象 `parse(String,ParsePosition)`、`parseObject`。
- 属性：`setCalendar/getCalendar`、`setNumberFormat/getNumberFormat`、`setTimeZone/getTimeZone`、
  `setLenient/isLenient`；`equals/hashCode/clone`。

## 2. Java 用户如何使用

- `DateFormat.getDateInstance(SHORT, Locale.US)` 格式化/解析日期；`setTimeZone(UTC)` 保证确定性；
  自定义子类实现抽象 format/parse 方法。

## 3. 参考来源（不凭记忆）

- Oracle Java SE 8 javadoc + `~/git/jdk8u-dev/jdk/src/share/classes/java/text/DateFormat.java`。
- JDK-27 同目录 DESIGN.md（差异：JDK-8 时间与 AM/PM 之间用普通空格，datetime 日期与时间之间无逗号，
  JDK-9+ 才用 U+202F 窄空格与逗号）。

## 4. 测试用例设计（映射到 DateFormatTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：abstract extends Format、常量值、工厂非空 | testWhiteBox |
| P0 | 各 style 的确定性 format 字符串（固定 Locale.US + UTC） | testFactoryFormatStrings |
| P0 | parse/lenient/setCalendar/setNumberFormat/setTimeZone | testParseLenientAndProperties |
| P0 | equals/clone/hashCode | testEqualsCloneHashCode |
| P0 | 受保护构造器子类 + parseObject + 边界 | testBlackBox |

## 关键规范细节（易错，已锁入断言）

- JDK-8 `getTimeInstance(SHORT, US)` 输出 `"12:00 AM"`（普通空格，非 U+202F）；
  `getDateTimeInstance(SHORT,SHORT,US)` 输出 `"1/1/70 12:00 AM"`（无逗号）。
- 字段常量值 ERA_FIELD=0、TIMEZONE_FIELD=17、DEFAULT=MEDIUM=2。

## 注意事项

- 固定 Locale.US + UTC + EPOCH；无墙钟/随机；headless-safe 不加 `@key`。
