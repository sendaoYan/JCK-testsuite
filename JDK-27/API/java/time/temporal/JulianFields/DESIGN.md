# JulianFields 测试设计案

## 1. API 功能
`JulianFields` 是提供儒略历相关日数计数的工具类（不可实例化），三个 `TemporalField` 常量：
- `JULIAN_DAY`：儒略日数（自公元前 4713-01-01 正午起算的天数）
- `MODIFIED_JULIAN_DAY`：修正儒略日（JULIAN_DAY - 2400000.5）
- `RATA_DIE`：自 0001-01-01 起算的天数
三个字段均为日期字段，可对 `LocalDate` 做 `getFrom/adjustInto/isSupportedBy`。

## 2. Java 用户如何使用
```java
long jd = JulianFields.JULIAN_DAY.getFrom(LocalDate.of(1970,1,1));   // 2440588
long rd = JulianFields.RATA_DIE.getFrom(LocalDate.of(1970,1,1));     // 719163
LocalDate back = JulianFields.JULIAN_DAY.adjustInto(LocalDate.of(2000,1,1), 2440588L); // 1970-01-01
```
组合链路：`isSupportedBy(date) → getFrom(date)`，`adjustInto(date, value)` 做日数→日期的反向换算。

## 3. 开源代码实际用法调研
- JDK 自身：`src/java.base/share/classes/java/time/format/DateTimeFormatterBuilder.java`（第 94 行 `import java.time.temporal.JulianFields`；第 1791 行 `g` 模式 `appendValue(JulianFields.MODIFIED_JULIAN_DAY, n, 19, SignStyle.NORMAL)`；第 2214 行 `FIELD_MAP.put('g', JulianFields.MODIFIED_JULIAN_DAY)`）——即 `g`/`gg` 格式化模式底层用 `MODIFIED_JULIAN_DAY`。
- JDK 测试：`test/jdk/java/time/tck/java/time/TCKYearMonth.java`、`TCKLocalDate.java`、`TCKLocalTime.java`、`TCKMonthDay.java`、`TCKZonedDateTime.java` 使用 `JulianFields` 校验跨字段一致性。
- 权威实现（范围与取值）：`src/java.base/share/classes/java/time/temporal/JulianFields.java`。

## 4. 测试用例设计 + 自动化实现
由 1–3 推导黑盒场景（`JulianFieldsTest.testBlackBox()`）：
- 纪元日数（1970-01-01）：`JULIAN_DAY=2440588`、`MODIFIED_JULIAN_DAY=40587`、`RATA_DIE=719163`
- 逐日递增：1970-01-02 三项分别 +1
- 分类/支持：`JULIAN_DAY.isDateBased()` 真、`isTimeBased()` 假；三字段均支持 `LocalDate`
- 往返：`JULIAN_DAY.adjustInto(date, 2440588)` 与 `RATA_DIE.adjustInto(date, 719163)` 均回到 1970-01-01
白盒部分（`testWhiteBox()`）：`final` 工具类、无公开构造器、三常量为 `TemporalField`。
