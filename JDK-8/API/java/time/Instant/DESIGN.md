# java.time.Instant 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
- 工厂：`EPOCH/MIN/MAX`、`now([Clock])`、`ofEpochSecond(long[,long])`、`ofEpochMilli`、`from`、`parse`。
- 查询：`getEpochSecond/getNano`、`isSupported/range/get/getLong/query`。
- 修改：`with(TemporalAdjuster|TemporalField,long)`、`truncatedTo`。
- 算术：`plus/minus`（TemporalAmount|TemporalUnit|Seconds/Millis/Nanos）。
- `until`、`atOffset/atZone/toEpochMilli`、`compareTo/isAfter/isBefore/equals/hashCode/toString`。
- `Serializable`（serialVersionUID -665713676816604388L）。

## 2. Java 用户如何使用
```java
Instant now = Instant.now();
Instant later = now.plusSeconds(60);
long epochSecond = later.getEpochSecond();
ZonedDateTime z = later.atZone(ZoneId.of("UTC"));
```

## 3. 开源代码实际用法调研
- `~/git/jdk8u-dev/jdk/test/java/time/tck/java/time/TCKInstant.java`（ofEpochSecond/truncatedTo/until 契约）。
- `~/git/jdk8u-dev/jdk/src/share/classes/java/time/Instant.java`（秒/纳秒存储、异常类型）。

## 4. 测试用例设计
- 工厂：EPOCH/MIN/MAX 极值、ofEpochSecond 纳秒滚动、ofEpochMilli（含 -1）、parse、from、now(固定 Clock)。
- 访问器：get(NANO_OF_SECOND)、getLong(INSTANT_SECONDS/MICRO/MILLI)、range、query；get(INSTANT_SECONDS)→DateTimeException、
  range/getLong(DAY_OF_MONTH)→UnsupportedTemporalTypeException。
- with/truncatedTo：截断到 SECONDS/MINUTES/HOURS/DAYS + 不可变性。
- 算术：plus/minus Seconds/Millis/Nanos（纳秒滚动）/Duration/unit/负值 + 不可变性。
- until：SECONDS/MILLIS/NANOS/负号/同值。
- 转换：atOffset/atZone 往返、toEpochMilli。
- 比较：compareTo 反对称、equals/hashCode、equals(null)/异类型。
- 序列化：serialVersionUID + round-trip（EPOCH/MIN/MAX 为缓存单例，不断言新实例）。
- 异常：ofEpochSecond 溢出→DateTimeException、parse 缺失 Z→DateTimeException、parse 垃圾→DateTimeParseException、
  plusSeconds 溢出→ArithmeticException、minusSeconds 溢出→DateTimeException、truncatedTo(MONTHS)→UnsupportedTemporalTypeException。
- null 语义：parse/from/now/plus/minus/with/until/atOffset/atZone → NPE。
- 确定性：固定 Instant/Clock/UTC；headless-safe（无 `@key`）。
