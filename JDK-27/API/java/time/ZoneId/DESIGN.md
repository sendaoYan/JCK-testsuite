# java.time.ZoneId 测试方案

## 1. API 功能（依据 JDK-27 `javap`）
- 构造/工厂：`of(String[,Map])`、`ofOffset(String,ZoneOffset)`、`systemDefault()`、`from`、`getAvailableZoneIds()`。
- 查询：`getId()`、`getDisplayName(TextStyle,Locale)`、`normalized()`、`getRules()`、`equals/hashCode/toString`。
- 抽象类：`ZoneId`（abstract），实现有 `ZoneRegion`（如 "UTC"/"Asia/Shanghai"）与 `ZoneOffset`。
- 常量：`SHORT_IDS`（静态 Map）。

## 2. Java 用户如何使用
```java
ZoneId z = ZoneId.of("Asia/Shanghai");
ZoneId utc = ZoneId.of("UTC");
ZoneId norm = utc.normalized();          // 归到 ZoneOffset.UTC
boolean same = z.equals(ZoneId.of("Asia/Shanghai"));
String name = z.getDisplayName(TextStyle.SHORT, Locale.US);
```

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/jdk/java/sql/JavatimeTest.java`：`ZonedDateTime.of(ldt, ZoneId.systemDefault())`。
- `~/git/jdk25u-dev/test/jdk/java/time/tck/java/time/TCKZoneId.java`（of/normalized/equals/getAvailableZoneIds）。
- `~/git/jdk25u-dev/src/java.base/share/classes/java/time/ZoneId.java`（SHORT_IDS 映射）。

## 4. 测试用例设计 + 自动化实现
对应 `ZoneIdTest.java` 的 `testBlackBox()`：
- of/getId/equals/hashCode、normalized（"UTC" 归一化到 ZoneOffset.UTC，但 `ZoneId.of("UTC")` 本身是 ZoneRegion）、SHORT_IDS（CST→America/Chicago）、getAvailableZoneIds 排序后稳定断言、systemDefault 类型、无效 id 抛 ZoneRulesException。
- 确定性：固定 id；headless-safe（`systemDefault` 只断言不依赖具体值）。
- 补充序列化：`serialVersionUID` 精确断言为 `8352817235686L`；`roundTrip` 往返 `ZoneId.of("UTC")`/`ZoneOffset.UTC`/`ZoneId.of("America/New_York")`/`ZoneOffset.ofHours(5)` 后 `.equals` 一致。
- 补充 null 语义：`getDisplayName(TextStyle.FULL, null)` 抛 NullPointerException。
