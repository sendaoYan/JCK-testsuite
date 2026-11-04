# java.util.SimpleTimeZone 测试设计

## 1. API 功能
- 构造器：`SimpleTimeZone(rawOffset, id)` 及带 DST 规则的长构造器。
- 偏移：`getRawOffset()`、`setRawOffset(int)`、`getOffset(era,y,m,d,dow,ms)`、`getOffset(long)`。
- DST：`setStartRule`/`setEndRule`（多种重载）、`setDSTSavings`、`getDSTSavings`、`useDaylightTime`、`observesDaylightTime`、`inDaylightTime`。
- 复制/相等：`clone()`、`equals`、`hashCode`、`hasSameRules`。

## 2. Java 用户如何使用
- 自定义固定偏移时区：`new SimpleTimeZone(3600000, "GMT+01:00")`。
- 自定义 DST 规则：`setStartRule(...)` + `setEndRule(...)` + `setDSTSavings(...)`。
- 查询某时刻偏移：`getOffset(era, y, m, d, dayOfWeek, millis)`。

## 3. 开源/源码实际用法调研
- JDK 源码 `java/util/TimeZone.java`：`getTimeZone(String)` 对自定义 ID 创建 `SimpleTimeZone`。
- JDK 自带测试 `test/jdk/java/util/TimeZone/`：SimpleTimeZone 的 DST 规则参照用例。
- 广泛用法（遗留系统）：用 `SimpleTimeZone` 表示非标准时区偏移。

## 4. 测试用例设计 + 自动化实现
- 白盒：`Class.forName` 断言具体类、继承 TimeZone、关键方法存在。
- 黑盒（固定日期/周几，不依赖平台默认时区）：
  - raw offset / setRawOffset / 无 DST 时 `useDaylightTime` false。
  - 设定 DST 规则后 `useDaylightTime` true、`getDSTSavings` 1 小时。
  - `getOffset` 在 7 月（DST）与 1 月（标准时）分别等于 raw+dst 与 raw。
  - clone/equals/hashCode/hasSameRules 组合。
