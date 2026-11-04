# java.time.MonthDay 测试方案

## 1. API 功能（依据 JDK-27 `javap`）
- 构造/工厂：`of(int,int)`、`of(Month,int)`、`now`、`parse`、`from`。
- 查询：`getMonthValue/getMonth/getDayOfMonth`、`isValidYear(int)`、`isSupported/range/get/getLong`。
- 核心操作：`withMonth/withDayOfMonth/with(Month)`、`with(TemporalAdjuster|TemporalField,long)`。
- 转换：`atYear(int)`、`format`、`adjustInto`。
- 比较/对象：`isBefore/isAfter`、`compareTo`、`equals/hashCode/toString`。

## 2. Java 用户如何使用
```java
MonthDay md = MonthDay.of(2, 29);          // 生日/纪念日（无年）
boolean valid2024 = md.isValidYear(2024);  // true
boolean valid2023 = md.isValidYear(2023);  // false
LocalDate d = md.atYear(2023);             // 2023-02-28（非闰年钳制）
MonthDay shifted = md.withMonth(1);
```

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/jdk/java/time/tck/java/time/TCKMonthDay.java`（of/parse/isValidYear/atYear 契约）。
- `~/git/jdk25u-dev/src/java.base/share/classes/java/time/MonthDay.java`（parse 仅接受 `--MM-dd`）。

## 4. 测试用例设计 + 自动化实现
对应 `MonthDayTest.java` 的 `testBlackBox()`：
- of(Month,int)/of(int,int)/parse("--02-29")/toString、getMonth/getMonthValue/getDayOfMonth、isValidYear、withMonth/withDayOfMonth/with(Month)、atYear（闰年精确、非闰年钳制到 2/28）、format、比较、越界（of(2,30)/of(13,1)/of(4,31)）与非法 parse（"02-30"）。
- 确定性：固定月日 + Locale.US；headless-safe。
