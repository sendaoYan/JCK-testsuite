# java.util.TimeZone 测试设计

## 1. API 功能
- 工厂：`getTimeZone(String)`、`getTimeZone(ZoneId)`。
- 偏移：`getRawOffset()`、`setRawOffset(int)`、`getOffset(long)`、`getOffset(era,y,m,d,dow,ms)`。
- 标识/规则：`getID()`、`setID`、`hasSameRules`、`useDaylightTime`、`getDSTSavings`。
- 枚举：`getAvailableIDs()`、`getAvailableIDs(int)`、`availableIDs()`。
- 转换：`toZoneId()`、`getDisplayName(...)`、`clone()`。

## 2. Java 用户如何使用
- 固定时区：`TimeZone.getTimeZone("UTC")`、`getTimeZone("GMT+05:00")`。
- 比较规则：`a.hasSameRules(b)`。
- 枚举零偏移：`getAvailableIDs(0)`。
- 与 java.time 互转：`tz.toZoneId()` / `TimeZone.getTimeZone(zoneId)`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/net/HttpCookie.java`：`getMaxAge()` 用 `TimeZone.getTimeZone("UTC")`。
- JDK 源码 `java/text/DateFormat.java`：`getTimeZone()`/`setTimeZone` 组合。
- JDK 源码 `java/text/SimpleDateFormat.java`：格式化时取 `TimeZone.getDefault()`。
- 广泛用法：`Calendar.getInstance(TimeZone.getTimeZone("UTC"))` 做 UTC 计算。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName` 断言抽象类、实现 Serializable/Cloneable、关键方法存在。
- 黑盒（只测 UTC/GMT 与固定偏移，不依赖平台默认时区）：
  - UTC/GMT raw offset 0、`hasSameRules`、`getOffset(epoch)==0`。
  - `GMT+05:00`/`GMT-05:00` 固定偏移与 `getOffset(epoch)`。
  - `getAvailableIDs` 非空、`getAvailableIDs(0)` 含 GMT。
  - `getTimeZone(ZoneId)`/`toZoneId` 往返、`getDisplayName(Locale.US)` 非空。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 契约深化：自定义偏移/未知ID/DST 判定/双 getOffset/默认区切换 | testTimeZoneContracts |
