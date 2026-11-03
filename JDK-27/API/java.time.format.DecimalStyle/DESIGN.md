# java.time.format.DecimalStyle

## 1. API 功能
本地化数字样式（数字、符号、小数分隔符）。`final` 值类型，通过常量/工厂获得，`withX` 返回新实例（不可变）。

- 工厂/常量：`STANDARD`、`of(Locale)`、`ofDefaultLocale()`、`getAvailableLocales()`
- 查询：`getZeroDigit()`、`getPositiveSign()`、`getNegativeSign()`、`getDecimalSeparator()`
- 不可变修改：`withZeroDigit(char)`、`withPositiveSign(char)`、`withNegativeSign(char)`、`withDecimalSeparator(char)`
- 其他：`equals`、`hashCode`、`toString`

## 2. Java 用户如何使用
```java
DecimalStyle s = DecimalStyle.STANDARD;
s.getZeroDigit();                 // '0'
s.getDecimalSeparator();          // '.'
DecimalStyle alt = s.withDecimalSeparator(','); // 新样式，原样不变
DecimalStyle.of(Locale.US).equals(DecimalStyle.STANDARD); // true
```

## 3. 开源代码实际用法调研（来源：JDK 源码 + test/jdk）
- `jdk25u-dev/src/java.base/share/classes/java/time/format/DateTimeFormatterBuilder.java`：数字打印/解析使用 `DecimalStyle` 的零位、符号、分隔符（`NumberPrinterParser`）。
- `jdk25u-dev/src/java.base/share/classes/java/time/format/DateTimePrintContext.java`：持有并传递 `DecimalStyle`。
- `jdk25u-dev/src/java.base/share/classes/java/time/format/DateTimeParseContext.java`：解析时使用 `DecimalStyle`。
- `jdk25u-dev/test/jdk/java/time/tck/java/time/format/TCKSignStyle.java`/`TCKPadPrinterParser.java`：间接覆盖 DecimalStyle 行为。

## 4. 测试用例设计 + 自动化实现
- 白盒：反射断言 `final`、非接口/抽象/枚举、父类 `Object`、`STANDARD` 字段存在。
- 黑盒：`STANDARD` 四 getter；`DecimalStyle.of(Locale.US)` 与 STANDARD 一致；四个 `withX` 均返回新样式且原样式不变；`getAvailableLocales()` 排序后含 `Locale.US`；`equals`/`hashCode`/`toString`。
- 注：`convertToDigit`/`convertNumberToIChar` 为包私有（javap `-public` 未列出），不纳入黑盒。
- 全部固定 `Locale.US`、headless-safe。
