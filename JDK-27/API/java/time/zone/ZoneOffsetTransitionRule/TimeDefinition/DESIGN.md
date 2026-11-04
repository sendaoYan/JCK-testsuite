# java.time.zone.ZoneOffsetTransitionRule.TimeDefinition

## 1. API 功能
嵌套枚举，定义转换时间 `LocalTime` 参照哪一套偏移来解释：

- `UTC`：以 UTC 偏移为准，实际本地时间 = 输入 + wall offset
- `STANDARD`：以标准偏移为准，实际本地时间 = 输入 + standard offset
- `WALL`：输入即"墙上时间"，直接返回

方法：`static TimeDefinition createDateTime(LocalDateTime, ZoneOffset standardOffset, ZoneOffset wallOffset)`、`values()`、`valueOf(String)`（继承自 Enum）。

## 2. Java 用户如何使用
```java
TimeDefinition.WALL.createDateTime(LocalDateTime.of(2020,1,1,0,0), std, wall); // 原样返回
TimeDefinition.UTC.createDateTime(dt, std, wall);       // dt + wall
TimeDefinition.STANDARD.createDateTime(dt, std, wall);  // dt + (wall - std)? 见实现：减去标准与墙差
```

## 3. 开源代码实际用法调研（来源：JDK 源码 + test/jdk）
- `jdk25u-dev/src/java.base/share/classes/java/time/zone/ZoneOffsetTransitionRule.java`：`createTransition` 内部按 `timeDefinition.createDateTime(...)` 计算转换瞬间。
- `jdk25u-dev/test/jdk/java/time/tck/java/time/zone/TCKZoneOffsetTransitionRule.java`：覆盖三种 TimeDefinition 的 `createDateTime` 行为（权威来源）。
- `jdk25u-dev/test/jdk/java/time/tck/java/time/zone/serial/TCKZoneOffsetTransitionRuleSerialization.java`：序列化含 TimeDefinition 字段。

## 4. 测试用例设计 + 自动化实现
- 白盒：反射断言 `isEnum`、`final`、父类 `Enum`、`getEnumConstants().length == 3`。
- 黑盒：`values().length == 3`；`valueOf` 三常量；`createDateTime(dt, std=+2, wall=+3)` 三种语义（UTC→03:00、STANDARD→01:00、WALL→原样）。
- 全部固定输入、headless-safe。
