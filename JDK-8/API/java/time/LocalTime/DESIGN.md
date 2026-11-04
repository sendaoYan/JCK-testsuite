# java.time.LocalTime 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
- 工厂：`MIN/MIDNIGHT/MAX/NOON`、`now([ZoneId|Clock])`、`of(int,int[,int[,int]])`、`ofSecondOfDay/ofNanoOfDay`、
  `from/parse([Formatter])`。
- 查询：`getHour/getMinute/getSecond/getNano`、`isSupported/range/get/getLong/query`。
- 修改：`with(...)`/`withHour/Minute/Second/Nano`、`truncatedTo`。
- 算术：`plus/minus`（TemporalAmount|TemporalUnit|Hours/Minutes/Seconds/Nanos）。
- `until`、`atDate/atOffset`、`toSecondOfDay/toNanoOfDay`、`format`、`compareTo/isAfter/isBefore/equals/hashCode/toString`。
- `Serializable`（serialVersionUID 6414437269572265201L）。

## 2. Java 用户如何使用
```java
LocalTime t = LocalTime.of(13, 45, 20);
LocalTime later = t.plusHours(1);              // 14:45:20
int seconds = t.toSecondOfDay();
```

## 3. 开源代码实际用法调研
- `~/git/jdk8u-dev/jdk/test/java/time/tck/java/time/TCKLocalTime.java`（of/truncatedTo/until 契约）。
- `~/git/jdk8u-dev/jdk/src/share/classes/java/time/LocalTime.java`（秒/纳秒跨日回绕、truncatedTo 上限）。

## 4. 测试用例设计
- 工厂：of 全重载、ofSecondOfDay/ofNanoOfDay 边界、parse、from、now(固定 Clock)、常量、toString。
- 查询：getters、toSecondOfDay/toNanoOfDay、isSupported/range/get/getLong/query；get(DAY_OF_MONTH)→UnsupportedTemporalTypeException。
- with/truncatedTo：with*/字段、truncatedTo 到秒/分/时 + 不可变性；truncatedTo(WEEKS)→UnsupportedTemporalTypeException。
- 算术：跨日回绕（plusNanos/plusSeconds/plusMinutes/plusHours/负值）+ 不可变性。
- until：HOURS/MINUTES/SECONDS/负号/同值。
- 转换：atDate/atOffset/format。
- 比较：compareTo/equals/hashCode/equals(null)/异类型。
- 序列化：serialVersionUID + round-trip。
- 异常：of 越界、ofSecondOfDay/ofNanoOfDay 越界、parse 垃圾。
- null 语义：parse/from/now/plus/minus/with/until/atDate/atOffset/isBefore/format → NPE。
- 确定性；headless-safe（无 `@key`）。
