# java.time.zone.ZoneOffsetTransitionRule

## 1. API 功能
描述一条"未来每年重复发生的" DST/偏移转换规则（如"每年三月最后一个周日 02:00"）。`final`、`Serializable`，无公开构造器。

- 工厂：`static of(Month, int dayOfMonthIndicator, DayOfWeek, LocalTime, boolean midnightEndOfDay, TimeDefinition, ZoneOffset standard, ZoneOffset before, ZoneOffset after)`
- 查询：`getMonth()`、`getDayOfMonthIndicator()`（负数表示"从月末倒数"）、`getDayOfWeek()`、`getLocalTime()`、`isMidnightEndOfDay()`、`getTimeDefinition()`、`getStandardOffset()`、`getOffsetBefore()`、`getOffsetAfter()`
- 转换：`createTransition(int year)`
- 其他：`equals`、`hashCode`、`toString`

## 2. Java 用户如何使用
```java
ZoneOffsetTransitionRule rule = ZoneOffsetTransitionRule.of(
    Month.MARCH, -1, DayOfWeek.SUNDAY, LocalTime.of(2, 0), false,
    TimeDefinition.WALL, ZoneOffset.ofHours(1), ZoneOffset.ofHours(1), ZoneOffset.ofHours(2));
ZoneOffsetTransition t = rule.createTransition(2024); // 2024-03-31T02:00
```

## 3. 开源代码实际用法调研（来源：JDK 源码 + test/jdk）
- `jdk25u-dev/src/java.base/share/classes/java/time/zone/ZoneRules.java`：`getTransitionRules()` 返回的规则列表由 tzdb 解析生成，`ZoneRules` 用 `createTransition` 计算未来转换。
- `jdk25u-dev/src/java.base/share/classes/java/time/zone/TzdbZoneRulesProvider.java`：从 tzdb 二进制读取规则并 `ZoneOffsetTransitionRule.of(...)`。
- `jdk25u-dev/test/jdk/java/time/tck/java/time/zone/TCKZoneOffsetTransitionRule.java`：TCK 覆盖 `of` 各参数、`createTransition`、相等性（权威行为来源）。
- `jdk25u-dev/test/jdk/java/time/tck/java/time/zone/serial/TCKZoneOffsetTransitionRuleSerialization.java`：序列化往返验证。

## 4. 测试用例设计 + 自动化实现
- 白盒：反射断言 `final`、非接口/抽象/枚举、父类 `Object`、`Serializable`、存在 9 参 `of`。
- 黑盒：固定规则（3 月 31 日、无星期约束）断言全部 getter；`createTransition(2024)` 的 `getDateTimeBefore`/`getOffsetBefore`/`getOffsetAfter`。
- 组合：负 `dayOfMonthIndicator`（-1）+ `DayOfWeek.SUNDAY` 求 2024 年三月最后一个周日 == 3 月 31 日。
- 相等性：两个等价规则 `equals`/`hashCode` 相等，与不同规则不等。
- 全部固定输入、无墙钟、无全局状态、headless-safe。
