# java.time.format.SignStyle

## 1. API 功能
数字格式化时符号输出的策略枚举：

- `NORMAL`：仅负数输出 '-'（默认）
- `ALWAYS`：正负都输出符号（'+'/'-'）
- `NEVER`：永不输出符号（负号被丢弃）
- `NOT_NEGATIVE`：禁止负数（负数抛异常）
- `EXCEEDS_PAD`：仅当数字宽度超出填充宽度时输出负号

## 2. Java 用户如何使用
```java
new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4, 10, SignStyle.ALWAYS)
    .toFormatter().format(Year.of(2024)); // "+2024"
```

## 3. 开源代码实际用法调研（来源：JDK 源码 + test/jdk）
- `jdk25u-dev/src/java.base/share/classes/java/time/format/DateTimeFormatterBuilder.java`：`NumberPrinterParser` 依据 SignStyle 决定符号输出。
- `jdk25u-dev/src/java.base/share/classes/java/time/format/DateTimeFormatter.java`：`appendValue` 默认 SignStyle.NORMAL。
- `jdk25u-dev/test/jdk/java/time/tck/java/time/format/TCKSignStyle.java`：TCK 覆盖各 SignStyle 的正负数字格式化（权威来源）。

## 4. 测试用例设计 + 自动化实现
- 白盒：反射断言 `isEnum`、`final`、父类 `Enum`、`getEnumConstants().length == 5`。
- 黑盒：`values().length == 5`、五个 `valueOf`；`appendValue(YEAR,4,10,style)` 分别断言：NORMAL 正/负、ALWAYS 正 `+2024`/负 `-2024`、NEVER 正负皆 `2024`、NOT_NEGATIVE 正 `2024`/负抛 `DateTimeException`、EXCEEDS_PAD 宽 6 时 `002024`/`-002024`（实际运行验证）。
- 全部固定输入、无墙钟、headless-safe。
