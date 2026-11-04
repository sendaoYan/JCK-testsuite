# java.time.Period 测试方案

## 1. API 功能（依据 JDK-8 `javap`，无 Comparable/compareTo）
- 工厂：`ZERO`、`ofYears/ofMonths/ofWeeks/ofDays/of(int,int,int)/from/parse/between`。
- 查询：`get(TemporalUnit)/getUnits/getChronology/isZero/isNegative/getYears/getMonths/getDays`。
- 修改：`withYears/withMonths/withDays`。
- 算术：`plus/minus`（TemporalAmount|Years/Months/Days）、`multipliedBy/negated/normalized/toTotalMonths`。
- `addTo/subtractFrom`、`equals/hashCode/toString`。
- `Serializable`（serialVersionUID -3587258372562876L）。

## 2. Java 用户如何使用
```java
Period p = Period.between(startDate, endDate);  // P2M5D
Period normalized = p.normalized();             // 月→年
long months = p.toTotalMonths();
```

## 3. 开源代码实际用法调研
- `~/git/jdk8u-dev/jdk/test/java/time/tck/java/time/TCKPeriod.java`（of/parse/normalized/toTotalMonths/between）。
- `~/git/jdk8u-dev/jdk/src/share/classes/java/time/Period.java`（年/月/日存储、multiplyExact 溢出）。

## 4. 测试用例设计
- 工厂：of/ofYears/ofMonths/ofWeeks(→天)/ofDays/from/parse（负值、周、round-trip）/between（正负）。
- get/getUnits/getChronology/getYears/Months/Days/get(HOURS)→UnsupportedTemporalTypeException。
- with/算术：with*/plus*/minus* 全重载 + 不可变性。
- multipliedBy/negated/normalized/toTotalMonths（含负月）。
- addTo/subtractFrom（月末钳制、闰年）。
- equals/hashCode/equals(null)/异类型。
- 序列化：serialVersionUID + round-trip。
- 异常：parse 垃圾、multipliedBy 溢出→ArithmeticException、get 不支持。
- null 语义：parse/from/between/plus/minus/addTo/subtractFrom → NPE。
- 确定性；headless-safe（无 `@key`）。
