# java.time.Duration 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
- 工厂：`ZERO`、`ofDays/ofHours/ofMinutes/ofSeconds(long[,long])/ofMillis/ofNanos/of(long,TemporalUnit)/from/parse/between`。
- 查询：`get(TemporalUnit)/getUnits/isZero/isNegative/getSeconds/getNano`。
- 修改：`withSeconds/withNanos`。
- 算术：`plus/minus`（Duration|TemporalUnit|Days/Hours/Minutes/Seconds/Millis/Nanos）、`multipliedBy/dividedBy/negated/abs`。
- `addTo/subtractFrom`、`toDays/toHours/toMinutes/toMillis/toNanos`、`compareTo/equals/hashCode/toString`。
- `Serializable`（serialVersionUID 3078945930695997490L）。

## 2. Java 用户如何使用
```java
Duration d = Duration.between(start, end);
Duration later = d.plusMinutes(5);
long seconds = d.getSeconds();
boolean negative = d.isNegative();
```

## 3. 开源代码实际用法调研
- `~/git/jdk8u-dev/jdk/test/java/time/tck/java/time/TCKDuration.java`（秒/纳秒存储、between 符号、dividedBy 精确除）。
- `~/git/jdk8u-dev/jdk/src/share/classes/java/time/Duration.java`（异常类型、除法/溢出）。

## 4. 测试用例设计
- 工厂：各 of* 归一化、ofSeconds 负值/纳秒、ofNanos 极值、of(amount,unit)、parse 复合/负、toString round-trip、between 符号。
- get/getUnits/getSeconds/getNano/get(DAYS)→UnsupportedTemporalTypeException。
- with/算术：plus/minus 全重载、负值、不可变性。
- multipliedBy/dividedBy（分数结果）/negated/abs/不可变性。
- addTo/subtractFrom Instant。
- toDays/toHours/toMinutes/toMillis/toNanos 截断（含负数）。
- 比较：compareTo/equals/hashCode/equals(null)/异类型。
- 序列化：serialVersionUID + round-trip。
- 异常：parse 垃圾、dividedBy 0、plusSeconds/multipliedBy/ofSeconds 溢出→ArithmeticException。
- null 语义：parse/from/of/between/plus/minus/addTo/subtractFrom → NPE。
- 确定性；headless-safe（无 `@key`）。
