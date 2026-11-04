# java.util.TimeZone 测试设计案（JDK-8）

> 覆盖目标：完整契约（Java 8 面）。**不改全局默认时区**；用 `getTimeZone("UTC"/"GMT"/"GMT±hh:mm")`
> 固定偏移区；抽象方法经 `SimpleTimeZone` 实例测试。

## 1. API 功能

- **工厂**：`getTimeZone(String)`、`getTimeZone(ZoneId)`、`getDefault()`、`getAvailableIDs()`、
  `getAvailableIDs(int)`。
- **偏移**：`getRawOffset()`、`setRawOffset(int)`、`getOffset(long)`、`getOffset(6 ints)`（抽象）。
- **标识**：`getID()`、`setID(String)`。
- **显示名**：`getDisplayName()`、`getDisplayName(Locale)`、`getDisplayName(boolean,int,Locale)`。
- **DST**：`getDSTSavings()`、`useDaylightTime()`、`observesDaylightTime()`、`inDaylightTime(Date)`。
- **等价**：`hasSameRules(TimeZone)`、`clone()`、`toZoneId()`。

## 2. 使用方式

`TimeZone.getTimeZone("UTC")`；`tz.getRawOffset()/getOffset(instant)`；`tz.getID()/setID()`；
`tz.getDisplayName(locale)`；`TimeZone.getAvailableIDs()`；`tz.toZoneId()`。

## 3. 参考来源

- Oracle Java SE 8 javadoc `java/util/TimeZone`。
- JDK 8 源码 `~/git/jdk8u-dev/jdk/src/share/classes/java/util/TimeZone.java`：
  未知 ID 回退到 "GMT"；`getTimeZone(null)`/`setID(null)` NPE。

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | UTC/GMT raw offset、getOffset(epoch)、hasSameRules、useDaylightTime | testUtcGmt |
| P0 | 固定偏移 GMT±05:00 raw offset + getOffset | testFixedOffsets |
| P0 | 未知 ID 回退 GMT + null NPE | testUnknownAndNull |
| P1 | getID/setID（null NPE）+ 显示名（固定 Locale） | testIdDisplayName |
| P1 | getAvailableIDs/getAvailableIDs(0)/getDefault | testAvailableIds |
| P1 | ZoneId 往返 + clone | testZoneIdAndClone |
| P0 | 契约深化：自定义偏移/未知ID/DST 判定/双 getOffset/默认区切换 | testTimeZoneContracts |

## 关键规范细节

- 未知 ID `getTimeZone("Not/AZone")` → 返回 ID 为 "GMT" 的时区。
- `getTimeZone((String)null)`/`getTimeZone((ZoneId)null)`/`setID(null)` → NPE。
- `useDaylightTime()`（UTC）为 false；`inDaylightTime(epoch)`（UTC）为 false。

## 注意事项

固定偏移区；**不调用 setDefault**；无墙钟/随机/全局副作用；headless-safe 不加 `@key`。
