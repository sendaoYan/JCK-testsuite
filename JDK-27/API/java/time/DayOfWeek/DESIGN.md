# java.time.DayOfWeek 测试方案

## 1. API 功能（依据 JDK-27 `javap`）
- 枚举：`DayOfWeek`（enum，MONDAY..SUNDAY），实现 `TemporalAccessor`、`TemporalAdjuster`。
- 查询：`getValue()`（ISO：周一=1..周日=7）、`plus(long)/minus(long)`、`getDisplayName(TextStyle,Locale)`、`adjustInto(Temporal)`、`from(TemporalAccessor)`。
- 静态工厂：`of(int)`、`valueOf`、`values`。

## 2. Java 用户如何使用
```java
DayOfWeek dow = DayOfWeek.of(1);      // MONDAY
DayOfWeek next = dow.plus(3);         // THURSDAY
DayOfWeek weekend = DayOfWeek.SUNDAY.minus(1); // SATURDAY
LocalDate d = dow.adjustInto(LocalDate.of(2024, 7, 1));
String name = dow.getDisplayName(TextStyle.FULL, Locale.US); // "Monday"
```

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/jdk/java/time/tck/java/time/TCKDayOfWeek.java`（of/plus/minus/from/displayName 契约）。
- `~/git/jdk25u-dev/src/java.base/share/classes/java/time/DayOfWeek.java`。

## 4. 测试用例设计 + 自动化实现
对应 `DayOfWeekTest.java` 的 `testBlackBox()`：
- values/of/getValue、plus/minus 回绕（SUNDAY.plus(1)==MONDAY）、adjustInto、from、getDisplayName、非法 of(0)/of(8)。
- 确定性：固定 Locale.US；headless-safe。
