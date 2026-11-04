# java.time.ZonedDateTime 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
- 工厂：`of(...)`（LocalDate+LocalTime+ZoneId、LocalDateTime+ZoneId、7 个 int）、`ofLocal/ofInstant/ofStrict`、
  `from/parse/now([ZoneId|Clock])`。
- 查询：`getOffset/getZone/toLocalDateTime/toLocalDate/toLocalTime`、date/time getters、
  `isSupported/range/get/getLong/query`。
- 修改：`withZoneSameInstant/withZoneSameLocal/withFixedOffsetZone/withEarlierOffsetAtOverlap/withLaterOffsetAtOverlap`、
  `with(...)`/`withYear/Month/DayOfMonth/DayOfYear/Hour/Minute/Second/Nano`、`truncatedTo`。
- 算术：`plus/minus`（TemporalAmount|TemporalUnit|Years/Months/Weeks/Days/Hours/Minutes/Seconds/Nanos）。
- `until`、`toOffsetDateTime/format`、`compareTo/isBefore/isAfter/isEqual/equals/hashCode/toString`。
- `Serializable`（serialVersionUID -6260982410461394882L）。

## 2. Java 用户如何使用
```java
ZonedDateTime z = ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
ZonedDateTime other = z.withZoneSameInstant(ZoneOffset.ofHours(1)); // 换时区保瞬时
```

## 3. 开源代码实际用法调研
- `~/git/jdk8u-dev/jdk/test/java/time/tck/java/time/TCKZonedDateTime.java`（of/withZoneSameInstant/until 契约）。
- `~/git/jdk8u-dev/jdk/src/share/classes/java/time/ZonedDateTime.java`（localDateTime+zone+offset 不可变）。

## 4. 测试用例设计
- 工厂：of 全重载、ofLocal/ofInstant/ofStrict、parse（含非零偏移）、from、now(固定 Clock)、toString。
- 查询：offset/zone/local 拆分、date/time getters、query(zone/offset)。
- 区域转换：withZoneSameInstant/withZoneSameLocal/withFixedOffsetZone/withEarlier/LaterOffsetAtOverlap + 不可变性。
- with/truncatedTo：with*/字段、truncatedTo 秒/天 + 不可变性。
- 算术：跨日/跨月回绕 + 月末钳制 + 不可变性。
- until：DAYS/HOURS/负号。
- 转换：toOffsetDateTime round-trip、format。
- 比较：compareTo/equals/hashCode/equals(null)/异类型。
- 序列化：serialVersionUID + round-trip。
- 异常：of 越界、ofStrict 偏移不匹配、plusYears 溢出、parse 无 zone/垃圾、get(INSTANT_SECONDS)→UnsupportedTemporalTypeException。
- null 语义：of/parse/from/now/withZone*/plus/minus/until/format → NPE。
- 确定性：固定偏移/UTC zone，不依赖 tzdata DST；headless-safe（无 `@key`）。
