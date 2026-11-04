# java.time.format.ResolverStyle

## 1. API 功能
解析日期时间时处理"字段冲突/越界"的策略枚举：

- `STRICT`：严格校验（如 2 月 30 日直接失败）
- `SMART`：智能解析（如 2 月 30 日→2 月 29 日）
- `LENIENT`：宽松解析（溢出顺延到下月）

## 2. Java 用户如何使用
```java
DateTimeFormatter.ofPattern("yyyy-MM-dd").withResolverStyle(ResolverStyle.STRICT)
    .parse("2024-02-30", LocalDate::from); // DateTimeParseException
DateTimeFormatter.ofPattern("yyyy-MM-dd").withResolverStyle(ResolverStyle.LENIENT)
    .parse("2024-02-30", LocalDate::from); // 2024-03-01
```

## 3. 开源代码实际用法调研（来源：JDK 源码 + test/jdk）
- `jdk25u-dev/src/java.base/share/classes/java/time/format/DateTimeFormatter.java`：`withResolverStyle(ResolverStyle)` 设置在 formatter 上。
- `jdk25u-dev/src/java.base/share/classes/java/time/format/Parsed.java`：按 ResolverStyle 执行字段解析（STRICT/SMART/LENIENT 分支）。
- `jdk25u-dev/test/jdk/java/time/tck/java/time/format/TCKResolverStyle.java`：TCK 覆盖三种 style 的解析（权威来源）。
- `jdk25u-dev/test/jdk/java/time/tck/java/time/format/TCKDateTimeParseResolver.java`：字段解析细节。

## 4. 测试用例设计 + 自动化实现
- 白盒：反射断言 `isEnum`、`final`、父类 `Enum`、`getEnumConstants().length == 3`。
- 黑盒：`values().length == 3`、三个 `valueOf`；STRICT 拒绝 "2024-02-30"（`DateTimeParseException`）；SMART→2024-02-29；LENIENT→2024-03-01；STRICT 拒绝 "2024-06-40"、LENIENT→2024-07-10。
- 全部固定输入（`yyyy-MM-dd`，无本地化文本）、无墙钟、headless-safe。
