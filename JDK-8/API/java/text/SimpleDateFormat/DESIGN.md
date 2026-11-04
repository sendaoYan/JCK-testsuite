# java.text.SimpleDateFormat 测试设计案（JDK-8）

> 覆盖目标：完整契约（构造/异常、模式字母、往返、两位年份、模式变更、符号/迭代器、equals/clone、
> 严格解析、序列化），非冒烟。

## 1. API 提供了哪些功能

- 构造器：`SimpleDateFormat()`、`SimpleDateFormat(String)`、`SimpleDateFormat(String,Locale)`、
  `SimpleDateFormat(String,DateFormatSymbols)`。
- 格式/解析：`format(Date,StringBuffer,FieldPosition)`、`formatToCharacterIterator(Object)`、
  `parse(String,ParsePosition)`（继承 `parse(String)`）。
- 模式：`toPattern()`/`toLocalizedPattern()`/`applyPattern(String)`/`applyLocalizedPattern(String)`。
- 符号/年份：`getDateFormatSymbols()`/`setDateFormatSymbols()`、`get2DigitYearStart()`/
  `set2DigitYearStart(Date)`。
- 契约：`clone()`、`equals()`、`hashCode()`；`Serializable`。
- JDK-8 的 `toString()` 继承 `Object.toString()`（不含 pattern，与 JDK-27 不同）。

## 2. Java 用户如何使用

- `new SimpleDateFormat("yyyy-MM-dd", Locale.US)` + `setTimeZone(UTC)` 后确定性 `format`/`parse`。
- `set2DigitYearStart` 控制两位年份世纪；`setDateFormatSymbols` 自定义月/星期名。

## 3. 参考来源（不凭记忆）

- Oracle Java SE 8 javadoc + `~/git/jdk8u-dev/jdk/src/share/classes/java/text/SimpleDateFormat.java`。
- JDK-27 同目录 DESIGN.md（JDK-8 实测：toString 不含 pattern）。

## 4. 测试用例设计（映射到 SimpleDateFormatTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：extends DateFormat、4 构造器、toPattern/formatToCharacterIterator、Serializable | testWhiteBox |
| P0 | 17 个模式字母/组合的确定性输出（Locale.US + UTC + epoch 0） | testPatternLetters |
| P0 | 非法 pattern（未闭合引号）→IAE；ctor(null)→NPE；format/parse null→NPE | testConstructorsAndExceptions |
| P0 | format/parse 往返 epoch；任意日期；垃圾输入→ParseException | testRoundTrip |
| P0 | 两位年份窗口 set2DigitYearStart | testTwoDigitYear |
| P0 | applyPattern/applyLocalizedPattern/toPattern/toLocalizedPattern；toString 非空 | testPatternMutation |
| P0 | formatToCharacterIterator + setDateFormatSymbols 生效 | testCharacterIteratorAndSymbols |
| P0 | equals/hashCode/clone（等 pattern+时区相等） | testEqualsCloneHashCode |
| P1 | 严格/宽松解析、闰日、ParsePosition 前进/错误索引 | testLenientStrictAndParsePosition |
| P1 | DateFormatSymbols 构造器 + no-arg 构造器；序列化 round-trip | testSymbolsCtorAndNoArg、testSerialization |

## 关键规范细节（易错，已锁入断言）

- 非法 pattern（未闭合单引号 `'`）抛 `IllegalArgumentException`。
- 严格模式下越界月份解析返回 null 且设置 errorIndex。
- `format((Date)null)`/`parse((String)null)` 抛 NPE。

## 注意事项

- 固定 Locale.US + UTC + Date(0)；无默认时区/墙钟/随机；headless-safe 不加 `@key`。
