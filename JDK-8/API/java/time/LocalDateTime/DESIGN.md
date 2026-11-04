# java.time.LocalDateTime 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
- 常量/工厂：`MIN/MAX`、`now([ZoneId|Clock])`、`of(int,int,int,int[,int[,int]])`、`of(int,Month,int,int[,int[,int]])`、
  `of(LocalDate,LocalTime)`、`ofInstant(Instant,ZoneId)`、`ofEpochSecond(long,int,ZoneOffset)`、
  `from/parse([Formatter])`。
- 查询：`toLocalDate/toLocalTime/getYear/getMonthValue/getMonth/getDayOfMonth/getDayOfYear/getDayOfWeek/getHour/getMinute/getSecond/getNano`、
  `isSupported/range/get/getLong/query`。
- 修改：`with(...)`/`withYear/Month/DayOfMonth/DayOfYear/Hour/Minute/Second/Nano`、`truncatedTo`。
- 算术：`plus/minus`（TemporalAmount|TemporalUnit|Years/Months/Weeks/Days/Hours/Minutes/Seconds/Nanos）。
- `until`、`atOffset/atZone`、`format`、`compareTo/isBefore/isAfter/isEqual/equals/hashCode/toString`。
- `Serializable`（serialVersionUID 6207766400415563566L）。

## 2. Java 用户如何使用
```java
LocalDateTime now = LocalDateTime.of(2024, 2, 29, 13, 45);
LocalDateTime later = now.plusHours(1);          // 14:45，跨日回绕
ZonedDateTime zdt = now.atZone(ZoneId.of("UTC")); // 带时区
```

## 3. 开源代码实际用法调研
- `~/git/jdk8u-dev/jdk/test/java/time/tck/java/time/TCKLocalDateTime.java`（of/plus/truncatedTo/until 契约）。
- `~/git/jdk8u-dev/jdk/src/share/classes/java/time/LocalDateTime.java`（date+time 组合不可变）。

## 4. 测试用例设计
- 工厂：of 全重载（5/6/7 参、Month 重载、LocalDate+LocalTime）、ofInstant/ofEpochSecond、parse、from、now(固定 Clock)、MIN/MAX、toString。
- 查询：toLocalDate/toLocalTime、date/time getters、getMonth/getDayOfWeek/getDayOfYear。
- temporal 访问器：isSupported/range/get/getLong/query；get(OFFSET_SECONDS)→UnsupportedTemporalTypeException。
- with/truncatedTo：with*/字段、truncatedTo 秒/天 + 不可变性。
- 算术：跨日/跨月回绕 + 月末钳制 + 不可变性。
- until：DAYS/HOURS/MINUTES/负号/同值。
- 转换：atOffset/atZone/format。
- 比较：compareTo/isBefore/isAfter/equals/hashCode/equals(null)/异类型。
- 序列化：serialVersionUID + round-trip。
- 异常：of 越界、parse 垃圾、plusYears 溢出。
- null 语义：of/parse/from/now/plus/minus/with/until/atOffset/atZone/format → NPE。
- 确定性；headless-safe（无 `@key`）。
