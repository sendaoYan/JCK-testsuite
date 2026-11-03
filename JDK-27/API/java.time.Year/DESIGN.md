# java.time.Year 测试方案

## 1. API 功能（依据 JDK-27 `javap`）
- 构造/工厂：`of(int)`、`now([ZoneId|Clock])`、`parse(CharSequence[,Formatter])`、`from`、`isLeap(long)`。
- 查询：`getValue()`、`isLeap()`、`length()`、`isBefore/isAfter`、`isSupported/range/get/getLong`。
- 核心操作：`plusYears/plus(long,TemporalUnit)/minusYears/minus`、`with(TemporalAdjuster|TemporalField,long)`、`until`。
- 转换：`atDay(int)`、`atMonth(Month|int)`、`atMonthDay(MonthDay)`、`format`、`adjustInto`。
- 比较/对象：`compareTo`、`equals/hashCode/toString`。
- 常量：`MIN_VALUE/MAX_VALUE`。

## 2. Java 用户如何使用
```java
Year y = Year.of(2024);
boolean leap = y.isLeap();            // true
int days = y.length();                // 366
LocalDate d = y.atMonthDay(MonthDay.of(2, 29)); // 2024-02-29
Year next = y.plusYears(4);
```

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/jdk/java/time/tck/java/time/TCKYear.java`（of/isLeap/length/atMonthDay/parse 契约）。
- `~/git/jdk25u-dev/src/java.base/share/classes/java/time/Year.java`（parse 宽松：YEAR 宽度 1..10）。

## 4. 测试用例设计 + 自动化实现
对应 `YearTest.java` 的 `testBlackBox()`：
- of/getValue/isLeap（1900/2000/2024/2023）、length、atDay/atMonth/atMonthDay、plusYears/minusYears、比较、parse（宽松、"garbage" 非法）、越界（of(0)/of(-1e9)）与 plusYears 溢出。
- 确定性：固定年份；headless-safe。
