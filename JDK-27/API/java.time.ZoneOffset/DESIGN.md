# java.time.ZoneOffset 测试方案

## 1. API 功能（依据 JDK-27 `javap`）
- 构造/工厂：`of(String)`、`ofHours/ofHoursMinutes/ofHoursMinutesSeconds/ofTotalSeconds`、`from`。
- 查询：`getTotalSeconds()`、`get(String)`、`getId()`、`getRules()`、`compareTo`、`equals/hashCode/toString`。
- 常量：`UTC`、`MIN/MAX`。
- 注意：`compareTo` 按偏移量**降序**（+18:00 在 -18:00 之前）。

## 2. Java 用户如何使用
```java
ZoneOffset off = ZoneOffset.of("+05:30");
int secs = off.getTotalSeconds();      // 19800
ZoneOffset byHours = ZoneOffset.ofHours(5);
boolean same = off.equals(byHours);
String id = off.getId();               // "+05:30"
```

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/jdk/java/sql/JavatimeTest.java`：`LocalDateTime.ofEpochSecond(secs, nanos, ZoneOffset.UTC)`。
- `~/git/jdk25u-dev/test/jdk/java/time/tck/java/time/TCKZoneOffset.java`（of/ofHours/getTotalSeconds/compareTo）。
- `~/git/jdk25u-dev/src/java.base/share/classes/java/time/ZoneOffset.java`。

## 4. 测试用例设计 + 自动化实现
对应 `ZoneOffsetTest.java` 的 `testBlackBox()`：
- of("+05:30")/of("+05")（无分钟合法）、ofHours/getTotalSeconds/getId、常量 UTC/MIN/MAX、equals/hashCode、compareTo **降序**（a.compareTo(b)>0 且 b.compareTo(a)<0）、无效（"05:00" 缺符号）抛 DateTimeException。
- 确定性：固定偏移；headless-safe。
