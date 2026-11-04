# java.util.GregorianCalendar 测试设计案（JDK-8）

> 覆盖目标：完整契约（Java 8 面）。固定 UTC + US；覆盖全部 7 个构造器、闰年规则（含世纪/400 年）、
> epoch 字段、add/roll、getActualMaximum 闰年、setGregorianChange(null NPE + 往返)、
> min/max/actual 范围、equals/hashCode、clone 独立、week-date。

## 1. API 功能

- **构造器（7）**：`()`、`(TimeZone)`、`(Locale)`、`(TimeZone,Locale)`、`(y,m,d)`、`(y,m,d,h,m)`、
  `(y,m,d,h,m,s)`。
- **日历切换**：`setGregorianChange(Date)`、`getGregorianChange()`。
- **闰年**：`isLeapYear(int)`。
- **类型**：`getCalendarType()`（"gregory"）。
- **运算**：`add`、`roll(boolean)`、`roll(int,int)`。
- **范围**：`getMinimum/getMaximum/getGreatestMinimum/getLeastMaximum/getActualMinimum/getActualMaximum`。
- **周日期**：`isWeekDateSupported/getWeekYear/setWeekDate/getWeeksInWeekYear`。

## 2. 使用方式

`new GregorianCalendar(tz, locale)` → `set(y,m,d,h,m,s)` → `get(field)` / `add(field,n)`；
`isLeapYear(year)`；`setWeekDate(weekYear, week, day)`。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/GregorianCalendar`。
- JDK 8 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/util/GregorianCalendar.java`：
  默认公历切换点、`setGregorianChange(null)` NPE、`isWeekDateSupported()` 返回 true。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 闰年规则（2000/1600/1900/2023） | testLeapYear |
| P0 | 7 个构造器字段 | testConstructors |
| P0 | epoch 字段 UTC | testEpoch |
| P0 | getCalendarType | testCalendarType |
| P0 | add 进位 vs roll 不进位 | testAddRoll |
| P0 | getActualMaximum 闰年/30 天月 | testActualMaximum |
| P0 | setGregorianChange 往返 + null NPE | testGregorianChange |
| P1 | min/max/actual 范围 | testMinMax |
| P1 | equals/hashCode + clone 独立 | testEqualsClone |
| P1 | week-date（isWeekDateSupported/getWeekYear/setWeekDate） | testWeekDate |

## 关键规范细节

- `isLeapYear`：能被 4 整除且（不能被 100 整除或能被 400 整除）——1600/2000 闰、1900 不闰。
- `setGregorianChange(null)` → NPE；`getGregorianChange()` 返回默认切换点（1582-10-15）。
- `isWeekDateSupported()` 返回 true；`setWeekDate` 后 `getWeekYear` 往返一致（ISO 周，周一起始 + minimalDays=4）。

## 注意事项

固定 UTC + US；无墙钟/随机/全局副作用；headless-safe 不加 `@key`。
