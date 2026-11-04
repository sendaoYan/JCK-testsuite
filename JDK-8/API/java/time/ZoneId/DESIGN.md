# java.time.ZoneId 测试方案

## 1. API 功能（依据 JDK-8 `javap`）
- 工厂：`of(String)`、`of(String,Map)`、`ofOffset(String,ZoneOffset)`、`from(TemporalAccessor)`、
  `systemDefault()`、`getAvailableZoneIds()`、`SHORT_IDS`。
- 查询：`getId()`、`getDisplayName(TextStyle,Locale)`、`getRules()`、`normalized()`。
- 对象：`equals/hashCode/toString`。
- `Serializable`（serialVersionUID 8352817235686L）。

## 2. Java 用户如何使用
```java
ZoneId zone = ZoneId.of("America/New_York");
ZoneOffset offset = zone.getRules().getOffset(Instant.now());
ZoneId utcOffset = zone.normalized();
```

## 3. 开源代码实际用法调研
- `~/git/jdk8u-dev/jdk/test/java/time/tck/java/time/TCKZoneId.java`（of/equality/normalized 契约）。
- `~/git/jdk8u-dev/jdk/src/share/classes/java/time/ZoneId.java`（ZoneOffset/ZoneRegion 子类型）。

## 4. 测试用例设计
- 工厂：of("UTC")/of("Z")==ZoneOffset.UTC/of 正负偏移/of 区域/of 短别名映射、ofOffset 前缀、from(ZonedDateTime/OffsetDateTime)、getAvailableZoneIds、systemDefault、SHORT_IDS。
- 查询：getDisplayName(固定偏移正/负)、getRules 非空/isFixedOffset。
- normalized：UTC→ZoneOffset.UTC、offset/region 归一为自身。
- 相等性：同区域相等、ZoneOffset.UTC.equals(of("Z"))、hashCode、equals(null)/异类型、toString。
- 序列化：serialVersionUID + round-trip。
- 异常：of 非法区域→DateTimeException、from(LocalDateTime)→DateTimeException、of/from/ofOffset null → NPE。
- null 语义：getDisplayName null Locale → NPE。
- 确定性；headless-safe（无 `@key`）。
