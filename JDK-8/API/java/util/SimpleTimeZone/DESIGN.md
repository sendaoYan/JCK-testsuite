# java.util.SimpleTimeZone 测试设计案（JDK-8）

> 覆盖目标：完整契约（Java 8 面）。固定 rawOffset/DST 规则；固定 Instant（用 UTC Calendar 计算毫秒）
> 测试 inDaylightTime/getOffset，不依赖默认时区。

## 1. API 功能

- **构造器**：`(rawOffset, ID)`、`(rawOffset, ID, startMonth, startDay, startDayOfWeek, startTime,
  endMonth, endDay, endDayOfWeek, endTime)`、带 savings 变体、带 start/end time-mode 变体。
- **偏移**：`getRawOffset()`、`setRawOffset(int)`、`getOffset(long)`、`getOffset(6 ints)`。
- **DST**：`setDSTSavings/getDSTSavings`、`setStartYear`、`setStartRule(4 重载)`、`setEndRule(4 重载)`、
  `useDaylightTime/observesDaylightTime/inDaylightTime`。
- **等价**：`clone/hashCode/equals/hasSameRules/toString`。

## 2. 使用方式

`new SimpleTimeZone(rawOffset, id)` → `setStartRule(...)`/`setEndRule(...)`/`setDSTSavings(...)` →
`getOffset(instant)`、`inDaylightTime(date)`。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/SimpleTimeZone`。
- JDK 8 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/util/SimpleTimeZone.java`。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | rawOffset + ID + 无 DST 规则 | testRawOffset |
| P0 | DST 规则 + getOffset(6 ints)/getOffset(long)/inDaylightTime | testDstRules |
| P0 | 三个 DST 规则构造器（savings/time-mode 变体） | testConstructorsWithRules |
| P0 | setStartRule/setEndRule 全重载 + setStartYear | testStartEndRuleOverloads |
| P0 | clone/equals/hashCode/hasSameRules/toString | testCloneEqualsHashCode |
| P0 | 契约深化：规则等价性/US 规则 DST/非法参数矩阵 | testSimpleTimeZoneContracts |

## 关键规范细节

- 无 DST 规则时 `useDaylightTime()` 为 false；设了 start/end 规则后为 true。
- `getOffset(6 ints)`：DST 期 = rawOffset + dstSavings；标准期 = rawOffset。
- `setEndRule(month, -1, dayOfWeek, time)` 表示「最后一个星期几」（dayOfMonth <= 0）。
- `inDaylightTime(Date)` 用固定 Instant 判断，与默认时区无关。

## 注意事项

固定 rawOffset/rules；固定 Instant（UTC Calendar 算毫秒）；无墙钟/随机/全局副作用；不加 `@key`。
