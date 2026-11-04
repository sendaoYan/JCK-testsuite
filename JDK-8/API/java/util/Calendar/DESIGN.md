# java.util.Calendar 测试设计案（JDK-8）

> 覆盖目标：完整契约（Java 8 面）。**固定 TimeZone/Locale**（UTC + US）消除 TZ/Locale 不确定性；
> 覆盖 epoch 字段、set 全重载、clear/isSet、add vs roll、lenient vs strict 的 IAE、
> firstDayOfWeek/minimalDays 校验、clone 独立、get/setTime 往返、序列化。

## 1. API 功能

- **工厂**：`getInstance()`、`getInstance(TimeZone)`、`getInstance(Locale)`、`getInstance(TimeZone,Locale)`。
- **字段访问**：`get(int)`、`set(int,int)`、`set(y,m,d)`、`set(y,m,d,h,m)`、`set(y,m,d,h,m,s)`、
  `clear()`、`clear(int)`、`isSet(int)`。
- **运算**：`add(int,int)`、`roll(int,boolean)`、`roll(int,int)`。
- **时间值**：`getTime()`、`setTime(Date)`、`getTimeInMillis()`、`setTimeInMillis(long)`。
- **顺序/相等**：`before/after/compareTo/equals/hashCode/clone`。
- **宽松/严格**：`setLenient/isLenient`。
- **周设置**：`setFirstDayOfWeek/getFirstDayOfWeek`、`setMinimalDaysInFirstWeek/getMinimalDaysInFirstWeek`。
- **范围**：`getMinimum/getMaximum/getGreatestMinimum/getLeastMaximum/getActualMinimum/getActualMaximum`。

## 2. 使用方式

`Calendar.getInstance(tz, locale)` → `clear()` → `set(y,m,d,h,m,s)` → `get(field)` / `add(field,n)`。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/Calendar`。
- JDK 8 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/util/Calendar.java`：non-lenient 下非法
  字段值 IAE；`setFirstDayOfWeek` 范围 SUNDAY..SATURDAY 外 IAE；`add` 与 `roll` 区别（roll 不进位）。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | epoch 字段（UTC）+ DAY_OF_WEEK + getTime | testEpoch |
| P0 | set 全重载（field/3/5/6 参） | testSet |
| P0 | clear/isSet 语义 | testClearIsSet |
| P0 | add 进位 vs roll 不进位 + 闰年钳制 | testAddRoll |
| P0 | before/after/compareTo/equals/hashCode + null NPE | testOrderingEquals |
| P0 | lenient vs strict（非法值 IAE） | testLenient |
| P1 | firstDayOfWeek/minimalDays 校验 | testWeekSettings |
| P1 | clone 独立 + setTimeZone | testCloneAndTimeZone |
| P1 | getMinimum/Maximum/getActualMaximum 闰年 | testMinMax |
| P1 | 序列化往返 + 复活字段完整 | testSerialization |
| P0 | 契约深化：字段常量/周参数/lenient 归一化/add-roll/极限/显示名 | testCalendarContracts |

## 关键规范细节

- non-lenient 下 `set(MONTH, 13)`/`set(DAY_OF_MONTH, 32)` → IAE；lenient 下归一化。
- `roll(MONTH, +1)` 不改变 YEAR；`add(MONTH, +1)` 会进位。
- `setFirstDayOfWeek`/`setMinimalDaysInFirstWeek` **不做校验**（值原样存储，set 后 get 往返）。
- `compareTo(null)` → NPE；`before(null)`/`after(null)` 返回 false（`when instanceof Calendar` 先判空，不抛）。
- 2020-02 的 `getActualMaximum(DAY_OF_MONTH) == 29`（闰年）。

## 注意事项

固定 UTC + US；无墙钟/随机/全局副作用；headless-safe 不加 `@key`。
