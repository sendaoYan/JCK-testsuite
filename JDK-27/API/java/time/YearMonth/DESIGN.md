# java.time.YearMonth 测试方案

## 1. API 功能（依据 JDK-27 `javap`）
- 构造/工厂：`of(int,int)`、`of(int,Month)`、`now`、`parse`、`from`。
- 查询：`getYear/getMonthValue/getMonth`、`isLeapYear`、`lengthOfMonth/lengthOfYear`、`atDay(int)`、`atEndOfMonth()`、`isSupported/range/get/getLong`。
- 核心操作：`plusYears/plusMonths/plus(long,TemporalUnit)/minus*`、`withYear/withMonth/with(TemporalAdjuster|TemporalField,long)`、`until`。
- 转换：`format`、`adjustInto`。
- 比较/对象：`isBefore/isAfter`、`compareTo`、`equals/hashCode/toString`。

## 2. Java 用户如何使用
```java
YearMonth ym = YearMonth.of(2024, 2);
int days = ym.lengthOfMonth();          // 29
LocalDate last = ym.atEndOfMonth();     // 2024-02-29
YearMonth next = ym.plusMonths(1);      // 2024-03
boolean leap = ym.isLeapYear();
```

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/jdk/java/time/tck/java/time/TCKYearMonth.java`：
```java
YearMonth.of(2008, 6);
YearMonth.of(2008, Month.FEBRUARY);
test.getMonth() == Month.DECEMBER;
```
- `~/git/jdk25u-dev/src/java.base/share/classes/java/time/YearMonth.java`。

## 4. 测试用例设计 + 自动化实现
对应 `YearMonthTest.java` 的 `testBlackBox()`：
- of/getYear/getMonthValue/getMonth、isLeapYear、lengthOfMonth/lengthOfYear、atDay/atEndOfMonth（含闰 2 月）、plusMonths/plusYears（跨年）、with、比较、parse、越界（of(0,1)/of(2024,13)）与非法 parse。
- 确定性：固定年月；headless-safe。
