# java.time.Month 测试方案

## 1. API 功能（依据 JDK-27 `javap`）
- 枚举：`Month`（enum，12 常量 JANUARY..DECEMBER），实现 `TemporalAccessor`、`TemporalAdjuster`。
- 查询：`getValue()`、`length(boolean leapYear)`、`minLength()/maxLength()`、`firstDayOfYear(boolean)`、`firstMonthOfQuarter()`、`plus(long)/minus(long)`、`getDisplayName(TextStyle,Locale)`、`adjustInto(Temporal)`、`from(TemporalAccessor)`。
- 静态工厂：`of(int)`、`valueOf`、`values`。

## 2. Java 用户如何使用
```java
Month m = Month.FEBRUARY;
int days = m.length(true);          // 29（闰年）
Month next = m.plus(1);             // MARCH
Month first = m.firstMonthOfQuarter();
LocalDate d = m.adjustInto(LocalDate.of(2024, 7, 15)); // 2024-02-15
String name = m.getDisplayName(TextStyle.SHORT, Locale.US); // "Feb"
```

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/jdk/java/time/tck/java/time/TCKMonth.java`（of/length/plus/firstMonthOfQuarter 契约）。
- `~/git/jdk25u-dev/test/jdk/java/time/tck/java/time/TCKYearMonth.java`：
```java
YearMonth.of(2008, Month.FEBRUARY);
test.getMonth() == Month.DECEMBER;
```
- `~/git/jdk25u-dev/src/java.base/share/classes/java/time/Month.java`。

## 4. 测试用例设计 + 自动化实现
对应 `MonthTest.java` 的 `testBlackBox()`：
- values/of/getValue/length（闰年 2 月 29）、min/maxLength、firstDayOfYear/firstMonthOfQuarter、plus/minus（含回绕）、adjustInto、getDisplayName（Locale.US）、from、非法 of(0)/of(13)。
- 确定性：固定 Locale.US；headless-safe。
