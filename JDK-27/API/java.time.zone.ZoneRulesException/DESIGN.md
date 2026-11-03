# java.time.zone.ZoneRulesException

## 1. API 功能
时区规则不可用/非法时抛出的运行时异常。继承 `java.time.DateTimeException`（→ `RuntimeException`）。

- 构造器：`(String message)`、`(String message, Throwable cause)`
- 继承方法：`getMessage()`、`getCause()`

## 2. Java 用户如何使用
```java
try {
    ZoneRulesProvider.getRules("Not/AZone", false);
} catch (ZoneRulesException e) {
    e.getMessage(); // "Unknown time-zone ID: Not/AZone"
}
```

## 3. 开源代码实际用法调研（来源：JDK 源码 + test/jdk）
- `jdk25u-dev/src/java.base/share/classes/java/time/zone/ZoneRulesProvider.java`：`getRules` 对未知 id 抛 `new ZoneRulesException("Unknown time-zone ID: " + zoneId)`。
- `jdk25u-dev/src/java.base/share/classes/java/time/zone/ZoneRulesException.java`：定义两个构造器。
- `jdk25u-dev/test/jdk/java/time/tck/java/time/zone/TCKZoneRulesProvider.java`：TCK 断言未知 zone 抛 `ZoneRulesException`。

## 4. 测试用例设计 + 自动化实现
- 白盒：反射断言非接口/抽象/枚举、父类 `DateTimeException`、是 `RuntimeException`、存在 `(String)` 与 `(String, Throwable)` 构造器（javap 确认无无参构造器）。
- 黑盒：`new ZoneRulesException("msg")` 的 `getMessage`、`instanceof DateTimeException/RuntimeException`；`new ZoneRulesException("msg", cause)` 的 `getCause`。
- 组合：`ZoneRulesProvider.getRules("Not/AZone", false)` 抛出 `ZoneRulesException`。
- 全部固定输入、headless-safe。
